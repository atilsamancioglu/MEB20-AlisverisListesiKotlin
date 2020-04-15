package com.atilsamancioglu.alisverislistesikotlin

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_liste.*
import java.lang.Exception


class ListeFragment : Fragment() {

    var isimListesi = ArrayList<String>()
    var idListesi = ArrayList<Int>()
    private lateinit var listeAdapter : ListeRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_liste, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listeAdapter = ListeRecyclerViewAdapter(isimListesi,idListesi)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = listeAdapter

        sqlVeriAl()
    }

    fun sqlVeriAl(){
        try {

            activity?.let {

                val database = activity!!.openOrCreateDatabase("Urunler", Context.MODE_PRIVATE,null)

                val cursor = database.rawQuery("SELECT * FROM urunler",null)
                val isimIndex = cursor.getColumnIndex("urunismi")
                val idIndex = cursor.getColumnIndex("id")

                isimListesi.clear()
                idListesi.clear()

                while (cursor.moveToNext()) {

                    //println(cursor.getString(isimIndex))
                    isimListesi.add(cursor.getString(isimIndex))
                    idListesi.add(cursor.getInt(idIndex))
                }

                listeAdapter.notifyDataSetChanged()

                cursor.close()

            }



        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}