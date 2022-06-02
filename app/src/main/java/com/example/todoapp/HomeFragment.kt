package com.example.todoapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.Model.TodoItem
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import java.text.DateFormat
import java.util.*


class HomeFragment : Fragment() {
    lateinit var recyclerView : RecyclerView
    lateinit var rootView: View
    lateinit var mAuth: FirebaseAuth
    lateinit var todosReference: DatabaseReference
    lateinit var statisticReference: DatabaseReference
    lateinit var mUser: FirebaseUser
    lateinit var onlineUserID: String
    lateinit var key:String
    lateinit var task: String
    lateinit var description: String
    var importance: Int = 1
    lateinit var thiscontext: Context


    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        rootView = inflater.inflate(R.layout.fragment_home, container, false)
        val toolbar = rootView.findViewById<Toolbar>(R.id.homeToolbar);
        toolbar.setTitle("Home");
        toolbar.inflateMenu(R.menu.main_menu);
        (requireActivity() as? HomeActivity)?.setSupportActionBar(toolbar)

        thiscontext = container!!.context

        recyclerView = rootView.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.setLayoutManager(LinearLayoutManager(activity))

        mAuth = FirebaseAuth.getInstance()
        var mUser = mAuth.currentUser
        onlineUserID = mUser!!.uid
        statisticReference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID)
        todosReference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineUserID).child("listOfTasks")

        val todoListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    rootView.findViewById<View>(R.id.empty_include).visibility = View.GONE
                }
                else{
                    rootView.findViewById<View>(R.id.empty_include).visibility = View.VISIBLE
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        }
        todosReference.addValueEventListener(todoListener)
        val options: FirebaseRecyclerOptions<TodoItem> = FirebaseRecyclerOptions.Builder<TodoItem>()
            .setQuery(todosReference, TodoItem::class.java)
            .build()
        val adapter =
            object : FirebaseRecyclerAdapter<TodoItem, TodoItemViewHolder>(options) {
                override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int, todoItem: TodoItem) {
                    holder.setDate(todoItem.date)
                    holder.setTask(todoItem.task)
                    holder.setDesc(todoItem.description)
                    holder.setImportance(todoItem.importance)
                    holder.mView.setOnClickListener {
                        key = getRef(position).getKey()!!
                        task = todoItem.task!!
                        description = todoItem.description!!
                        importance = todoItem.importance!!
                        updateTask()
                    }
                    holder.mView.findViewById<ImageView>(R.id.deleteTask).setOnClickListener {
                        key = getRef(position).getKey()!!
                        deleteTask()
                    }
                }
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.retrieved_layout, parent, false)
                    return TodoItemViewHolder(view)
                }
            }


        recyclerView.setAdapter(adapter)

        adapter.startListening()

        return rootView

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)

    }
    fun deleteTask() {
        val myDialog = AlertDialog.Builder(thiscontext)
        val inflater = LayoutInflater.from(thiscontext)
        val view = inflater.inflate(R.layout.delete_data, null)
        myDialog.setView(view)
        val dialog = myDialog.create()


        val delButton = view.findViewById<Button>(R.id.btnDeleteTask)
        val noButton = view.findViewById<Button>(R.id.btnNo)
        delButton.setOnClickListener {
            todosReference.child(key).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    statisticReference.child("failedTasks").get().addOnSuccessListener {
                        statisticReference.child("failedTasks").setValue(it.value.toString().toInt() + 1)
                    }
                    Toast.makeText(
                        thiscontext,
                        "Task deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val err = task.exception.toString()
                    Toast.makeText(
                        thiscontext,
                        "Failed to delete task $err",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            dialog.dismiss()
        }
        noButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
    fun updateTask() {
        val myDialog = AlertDialog.Builder(thiscontext)
        val inflater = LayoutInflater.from(thiscontext)
        val view = inflater.inflate(R.layout.update_data, null)
        myDialog.setView(view)
        val dialog = myDialog.create()
        val mTask = view.findViewById<EditText>(R.id.mEditTextTask)
        val mDescription = view.findViewById<EditText>(R.id.mEditTextDescription)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioButtonGroup)
        mTask.setText(task)
        mTask.setSelection(task.length)
        mDescription.setText(description)
        mDescription.setSelection(description.length)

        val delButton = view.findViewById<Button>(R.id.btnDelete)
        val updateButton = view.findViewById<Button>(R.id.btnUpdate)

        updateButton.setOnClickListener {
            task = mTask.text.toString().trim { it <= ' ' }
            description = mDescription.text.toString().trim { it <= ' ' }

            when (radioGroup.checkedRadioButtonId) {
                R.id.radioRed -> {
                    importance = 1
                }
                R.id.radioYellow -> {
                    importance = 2
                }
                R.id.radioGreen -> {
                    importance = 3
                }
            }
            val date = DateFormat.getDateInstance().format(Date())
            val model = TodoItem(task, description, key, date,importance)
            todosReference.child(key).setValue(model).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        thiscontext,
                        "Data has been updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val err = task.exception.toString()
                    Toast.makeText(thiscontext, "update failed $err", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            dialog.dismiss()
        }
        delButton.setOnClickListener {
            todosReference.child(key).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    statisticReference.child("completedTasks").get().addOnSuccessListener {
                        statisticReference.child("completedTasks").setValue(it.value.toString().toInt() + 1)
                    }
                    Toast.makeText(
                        thiscontext,
                        "Task completed",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val err = task.exception.toString()
                    Toast.makeText(
                        thiscontext,
                        "Failed to delete task $err",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            dialog.dismiss()
        }
        dialog.show()
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu?.findItem(R.id.app_bar_search)
        searchItem?.expandActionView()
        val searchView = searchItem!!.actionView as SearchView
        val searchHint = getString(R.string.search)
        searchView.setQueryHint(searchHint)
        if (searchItem!=null) {
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if (newText!!.toString().isNotEmpty()) {
                        searchInDatabase(newText)

                    }
                    else {
                        searchInDatabase(newText)

                    }
                    return false
                }
            })
        }
        super.onCreateOptionsMenu(menu, inflater)

    }
    private fun searchInDatabase(newText: String) {
        var query: Query = todosReference.orderByChild("task")
            .startAt(newText)
            .endAt(newText+"\uf8ff")
        query.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val options: FirebaseRecyclerOptions<TodoItem> = FirebaseRecyclerOptions.Builder<TodoItem>()
                    .setQuery(query, TodoItem::class.java)
                    .build()
                val adapter =
                    object : FirebaseRecyclerAdapter<TodoItem, TodoItemViewHolder>(options) {
                        override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int, todoItem: TodoItem) {
                            holder.setDate(todoItem.date)
                            holder.setTask(todoItem.task)
                            holder.setDesc(todoItem.description)
                            holder.setImportance(todoItem.importance)
                            holder.mView.setOnClickListener {
                                key = getRef(position).getKey()!!
                                task = todoItem.task!!
                                description = todoItem.description!!
                                importance = todoItem.importance!!
                                updateTask()
                            }
                        }

                        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
                            val view = LayoutInflater.from(parent.context)
                                .inflate(R.layout.retrieved_layout, parent, false)
                            return TodoItemViewHolder(view)
                        }
                    }
                recyclerView.setAdapter(adapter)
                adapter.startListening()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                mAuth.signOut()
                val intent = Intent(thiscontext, LoginActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            R.id.app_bar_sort_by_importance ->{
                var query: Query = todosReference.orderByChild("importance")
                query.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val options: FirebaseRecyclerOptions<TodoItem> = FirebaseRecyclerOptions.Builder<TodoItem>()
                            .setQuery(query, TodoItem::class.java)
                            .build()
                        val adapter =
                            object : FirebaseRecyclerAdapter<TodoItem, TodoItemViewHolder>(options) {
                                override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int, todoItem: TodoItem) {
                                    holder.setDate(todoItem.date)
                                    holder.setTask(todoItem.task)
                                    holder.setDesc(todoItem.description)
                                    holder.setImportance(todoItem.importance)
                                    holder.mView.setOnClickListener {
                                        key = getRef(position).getKey()!!
                                        task = todoItem.task!!
                                        description = todoItem.description!!
                                        importance = todoItem.importance!!
                                        updateTask()
                                    }
                                }

                                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
                                    val view = LayoutInflater.from(parent.context)
                                        .inflate(R.layout.retrieved_layout, parent, false)
                                    return TodoItemViewHolder(view)
                                }
                            }
                        recyclerView.setAdapter(adapter)
                        adapter.startListening()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            }
            R.id.app_bar_sort_by_date ->{
                var query: Query = todosReference.orderByChild("date")
                query.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val options: FirebaseRecyclerOptions<TodoItem> = FirebaseRecyclerOptions.Builder<TodoItem>()
                            .setQuery(query, TodoItem::class.java)
                            .build()
                        val adapter =
                            object : FirebaseRecyclerAdapter<TodoItem, TodoItemViewHolder>(options) {
                                override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int, todoItem: TodoItem) {
                                    holder.setDate(todoItem.date)
                                    holder.setTask(todoItem.task)
                                    holder.setDesc(todoItem.description)
                                    holder.setImportance(todoItem.importance)
                                    holder.mView.setOnClickListener {
                                        key = getRef(position).getKey()!!
                                        task = todoItem.task!!
                                        description = todoItem.description!!
                                        importance = todoItem.importance!!
                                        updateTask()
                                    }
                                }

                                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
                                    val view = LayoutInflater.from(parent.context)
                                        .inflate(R.layout.retrieved_layout, parent, false)
                                    return TodoItemViewHolder(view)
                                }
                            }
                        recyclerView.setAdapter(adapter)
                        adapter.startListening()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }
        }
        return super.onOptionsItemSelected(item)
    }
    companion object {
        fun newInstance() = HomeFragment()
    }
}