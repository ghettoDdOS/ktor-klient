package com.example.ktor_klient

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ktor_klient.adapters.FacultyItemViewAdapter
import com.example.ktor_klient.api.ApiFactory
import com.example.ktor_klient.api.resources.Faculties
import com.example.ktor_klient.components.SwipeActionsCallback
import com.example.ktor_klient.databinding.ActivityMainBinding
import com.example.ktor_klient.databinding.FacultyEditBinding
import com.example.ktor_klient.models.Chair
import com.example.ktor_klient.models.Faculty
import com.google.android.material.snackbar.Snackbar
import io.ktor.client.call.*
import io.ktor.client.plugins.resources.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val mainScope = MainScope()

    private lateinit var mAdapter: FacultyItemViewAdapter
    private lateinit var b: ActivityMainBinding
    private val facultyItemsList: MutableList<Faculty> = mutableListOf()
    var recyclerView: RecyclerView? = null
    var constraintLayout: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        mainScope.launch {
            fetchFaculties()
        }
        setUpAdapter()
        enableSwipeToDeleteAndUndo()
    }

    private fun setUpAdapter() {
        recyclerView = findViewById(R.id.FacultiesList)
        constraintLayout = findViewById(R.id.constraintLayout)
        mAdapter = FacultyItemViewAdapter(this,facultyItemsList)
        b.FacultiesList.adapter = mAdapter
        b.FacultiesList.layoutManager = LinearLayoutManager(this)
    }

    private suspend fun  fetchFaculties() {
        val api = ApiFactory.getClient()
        kotlin.runCatching {
            api.get(Faculties()).body<List<Faculty>>()
        }.onSuccess {
            facultyItemsList.addAll(it)
            mAdapter.notifyDataSetChanged()
        }.onFailure {
            Log.e("REQUEST", it.toString())
            Toast.makeText(this, "Ошибка сервера!", Toast.LENGTH_LONG).show()
        }
    }

    private fun enableSwipeToDeleteAndUndo() {
        val swipeActionsCallback: SwipeActionsCallback = object : SwipeActionsCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = mAdapter.getData()[position]
                if (direction == ItemTouchHelper.LEFT){
                    mAdapter.removeItem(position)
                    val restoreAction = Snackbar
                        .make(
                            constraintLayout!!,
                            "Элемент удален.",
                            Snackbar.LENGTH_LONG
                        )
                    restoreAction.setAction("ОТМЕНИТЬ") {
                        mAdapter.restoreItem(item, position)
                        recyclerView!!.scrollToPosition(position)
                    }
                    restoreAction.setActionTextColor(Color.YELLOW)
                    restoreAction.show()
                } else if(direction == ItemTouchHelper.RIGHT) {
                    val dialogBuilder = AlertDialog.Builder(this@MainActivity)
                    val dialogBinding = FacultyEditBinding.inflate(layoutInflater)

                    var nameInput = dialogBinding.root.findViewById<EditText>(R.id.name)
                    var shortNameInput = dialogBinding.root.findViewById<EditText>(R.id.shortName)


                    dialogBuilder.setView(R.layout.faculty_edit)
                    dialogBuilder.setTitle("Редактировать факультет")
                    dialogBuilder.setPositiveButton("Сохранить") { dialog, _ ->
                        Log.i("EDIT", "SAVE")
                        dialog.cancel()
                    }
                    dialogBuilder.setNegativeButton("Отменить") { dialog, _ -> dialog.cancel()}
                    val b = dialogBuilder.create()
                    b.show()

                }
                recyclerView?.adapter?.notifyItemChanged(position)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeActionsCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}