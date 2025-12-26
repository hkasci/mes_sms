package com.example.myapplication.mes_sms

import com.example.myapplication.R

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.mes_sms.data.AppDatabase
import com.example.myapplication.mes_sms.data.ContactRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ContactListActivity : AppCompatActivity() {
    private lateinit var rv: RecyclerView
    private lateinit var btnAdd: Button
    private lateinit var btnFilter: Button
    private lateinit var btnSendSelected: Button
    private var filtered = mutableListOf<Contact>()
    private val selected = mutableSetOf<String>() // phone numbers
    private lateinit var adapter: ContactAdapter
    private lateinit var repo: ContactRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        val db = AppDatabase.getInstance(this)
        repo = ContactRepository(db.contactDao())

        rv = findViewById(R.id.rvContacts)
        btnAdd = findViewById(R.id.btnAddContact)
        btnFilter = findViewById(R.id.btnFilter)
        btnSendSelected = findViewById(R.id.btnSendSelected)

        adapter = ContactAdapter(filtered,
            onDelete = { pos ->
                val toRemove = filtered[pos]
                runBlocking { repo.deleteByPhone(toRemove.phoneNumber) }
                selected.remove(toRemove.phoneNumber)
            },
            onSend = { pos ->
                val c = filtered[pos]
                promptMessageAndSend(c)
            },
            onToggleSelect = { pos ->
                val c = filtered[pos]
                if (selected.contains(c.phoneNumber)) selected.remove(c.phoneNumber) else selected.add(c.phoneNumber)
                adapter.notifyItemChanged(pos)
            },
            isSelected = { pos -> filtered[pos].phoneNumber in selected }
        )

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        lifecycleScope.launch {
            repo.contactsFlow.collectLatest { list ->
                filtered.clear()
                filtered.addAll(list)
                adapter.notifyDataSetChanged()
            }
        }

        btnAdd.setOnClickListener { showAddDialog() }
        btnFilter.setOnClickListener { showFilterDialog() }
        btnSendSelected.setOnClickListener { promptSendSelected() }
    }

    private fun promptSendSelected() {
        if (selected.isEmpty()) {
            AlertDialog.Builder(this).setMessage("No contacts selected").setPositiveButton("OK", null).show()
            return
        }
        val input = EditText(this)
        input.hint = getString(R.string.default_emergency_message)
        AlertDialog.Builder(this)
            .setTitle("Send to selected (${selected.size})")
            .setView(input)
            .setPositiveButton("Send") { dlg, _ ->
                val msg = input.text.toString()
                selected.forEach { phone -> SmsManagerHelper.sendSms(this, phone, msg) }
                dlg.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showFilterDialog() {
        val options = arrayOf("All", "By City", "By Department")
        AlertDialog.Builder(this)
            .setTitle("Filter contacts")
            .setItems(options) { dlg, which ->
                when (which) {
                    0 -> { lifecycleScope.launch { val list = repo.getAll(); filtered.clear(); filtered.addAll(list); adapter.notifyDataSetChanged() } }
                    1 -> showInputFilter("city")
                    2 -> showInputFilter("department")
                }
                dlg.dismiss()
            }
            .show()
    }

    private fun showInputFilter(kind: String) {
        val input = EditText(this)
        input.hint = "Enter $kind"
        AlertDialog.Builder(this)
            .setTitle("Filter by $kind")
            .setView(input)
            .setPositiveButton("OK") { dlg, _ ->
                val q = input.text.toString().trim()
                lifecycleScope.launch {
                    val all = repo.getAll()
                    filtered.clear()
                    if (kind == "city") filtered.addAll(all.filter { it.city.equals(q, true) })
                    else filtered.addAll(all.filter { it.department.equals(q, true) })
                    adapter.notifyDataSetChanged()
                }
                dlg.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun promptMessageAndSend(contact: Contact) {
        val input = EditText(this)
        input.hint = getString(R.string.default_emergency_message)
        AlertDialog.Builder(this)
            .setTitle("Send to ${contact.firstName}")
            .setView(input)
            .setPositiveButton("Send") { dlg, _ ->
                val msg = input.text.toString()
                SmsManagerHelper.sendSms(this, contact.phoneNumber, msg)
                dlg.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddDialog() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val etFirst = EditText(this)
        etFirst.hint = "First name"
        val etLast = EditText(this)
        etLast.hint = "Last name"
        val etDept = EditText(this)
        etDept.hint = "Department"
        val etCity = EditText(this)
        etCity.hint = "City"
        val etPhone = EditText(this)
        etPhone.hint = "Phone number"
        etPhone.inputType = InputType.TYPE_CLASS_PHONE
        layout.addView(etFirst)
        layout.addView(etLast)
        layout.addView(etDept)
        layout.addView(etCity)
        layout.addView(etPhone)

        AlertDialog.Builder(this)
            .setTitle("Add Contact")
            .setView(layout)
            .setPositiveButton("Add") { dlg, _ ->
                val c = Contact(
                    id = System.currentTimeMillis(),
                    firstName = etFirst.text.toString(),
                    lastName = etLast.text.toString(),
                    department = etDept.text.toString(),
                    city = etCity.text.toString(),
                    phoneNumber = etPhone.text.toString()
                )
                lifecycleScope.launch { repo.insert(c) }
                dlg.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    class ContactAdapter(
        private val items: List<Contact>,
        private val onDelete: (Int) -> Unit,
        private val onSend: (Int) -> Unit,
        private val onToggleSelect: (Int) -> Unit,
        private val isSelected: (Int) -> Boolean
    ) : RecyclerView.Adapter<ContactAdapter.VH>() {
        class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvName)
            val tvPhone: TextView = view.findViewById(R.id.tvPhone)
            val cbSelect: CheckBox? = view.findViewById(R.id.cbSelect)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val c = items[position]
            holder.tvName.text = "${c.firstName} ${c.lastName}"
            holder.tvPhone.text = c.phoneNumber
            holder.itemView.setOnLongClickListener {
                AlertDialog.Builder(it.context)
                    .setMessage("Delete contact ${c.firstName}?")
                    .setPositiveButton("Delete") { _, _ -> onDelete(position) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
            holder.itemView.setOnClickListener {
                onSend(position)
            }
            holder.cbSelect?.isChecked = isSelected(position)
            holder.cbSelect?.setOnClickListener { onToggleSelect(position) }
        }

        override fun getItemCount(): Int = items.size
    }
}
