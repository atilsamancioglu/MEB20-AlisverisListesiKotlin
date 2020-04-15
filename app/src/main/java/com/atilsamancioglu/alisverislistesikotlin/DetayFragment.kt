package com.atilsamancioglu.alisverislistesikotlin

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_detay.*
import java.io.ByteArrayOutputStream
import java.lang.Exception


class DetayFragment : Fragment() {

    var secilenGorsel : Uri? = null
    var secilenBitmap : Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detay, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener { kaydet(view) }
        imageView.setOnClickListener { gorselSec(view) }

        arguments?.let {
            val bilgi = DetayFragmentArgs.fromBundle(it).bilgi

            if (bilgi.equals("yeni")) {
                // yeni bir ürün eklenecek

                urunFiyatText.setText("")
                urunIsimText.setText("")
                button.visibility = View.VISIBLE

                val gorselSecArkaPlan = BitmapFactory.decodeResource(context?.resources,R.drawable.gorselsec)
                imageView.setImageBitmap(gorselSecArkaPlan)


            } else {
                //recycler'dan seçilen eski ürün gösterilecek

                button.visibility = View.INVISIBLE

                val secilenId = DetayFragmentArgs.fromBundle(it).id

                val database = context?.openOrCreateDatabase("Urunler",Context.MODE_PRIVATE,null)

                val cursor = database!!.rawQuery("SELECT * FROM urunler WHERE id = ?", arrayOf(secilenId.toString()))

                val urunIsmiIndex = cursor.getColumnIndex("urunismi")
                val urunFiyatiIndex = cursor.getColumnIndex("urunfiyati")
                val gorselIndex = cursor.getColumnIndex("gorsel")

                while (cursor.moveToNext()) {
                    urunFiyatText.setText(cursor.getString(urunFiyatiIndex))
                    urunIsimText.setText(cursor.getString(urunIsmiIndex))

                    val byteDizisi = cursor.getBlob(gorselIndex)
                    val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                    imageView.setImageBitmap(bitmap)
                }

                cursor.close()
            }

        }


    }

    fun kaydet( view : View) {

        val urunIsmi = urunIsimText.text.toString()
        val urunFiyat = urunFiyatText.text.toString()
        if (secilenBitmap != null) {
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,300)

            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            try {
                val database = context?.openOrCreateDatabase("Urunler", Context.MODE_PRIVATE,null)
                database?.execSQL("CREATE TABLE IF NOT EXISTS urunler (id INTEGER PRIMARY KEY, urunismi VARCHAR, urunfiyati VARCHAR, gorsel BLOB)")

                val sqlString = "INSERT INTO urunler (urunismi, urunfiyati, gorsel) VALUES (?, ?, ?)"
                val statement = database!!.compileStatement(sqlString)
                statement.bindString(1,urunIsmi)
                statement.bindString(2,urunFiyat)
                statement.bindBlob(3,byteDizisi)

                statement.execute()

            } catch (e : Exception) {
                e.printStackTrace()
            }

            val action = DetayFragmentDirections.actionDetayFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)

        }

    }

    fun kucukBitmapOlustur(kullaniciSecilenBitmap: Bitmap,maximumBoyut:Int):Bitmap{

        var width = kullaniciSecilenBitmap.width
        var height = kullaniciSecilenBitmap.height

        val bitmapOrani : Double = width.toDouble() / height.toDouble()

        if (bitmapOrani > 1) {
            //yatay
            width = maximumBoyut
            val kisaltilmisUzunluk = width / bitmapOrani
            height = kisaltilmisUzunluk.toInt()
        } else {
            //dikey
            height = maximumBoyut
            val kisaltilmisGenislik = height * bitmapOrani
            width = kisaltilmisGenislik.toInt()
        }

        return Bitmap.createScaledBitmap(kullaniciSecilenBitmap,width,height,true)
    }

    fun gorselSec(view: View){

        activity?.let {

            if(ContextCompat.checkSelfPermission(activity!!.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //izin verilmemişse -> izin iste
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            } else {
                // izin zaten verilmiş -> direk galeriye git
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }


        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //kullanıcı izni verdi
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)
            }
        }



        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {

            secilenGorsel = data.data

            try {

                if (secilenGorsel != null) {

                    if (Build.VERSION.SDK_INT >= 28) {
                        val source = ImageDecoder.createSource(context!!.contentResolver,secilenGorsel!!)
                        secilenBitmap = ImageDecoder.decodeBitmap(source)
                        imageView.setImageBitmap(secilenBitmap)
                    } else {
                        secilenBitmap = MediaStore.Images.Media.getBitmap(context!!.contentResolver,secilenGorsel)
                        imageView.setImageBitmap(secilenBitmap)
                    }
                }
            } catch (e : Exception) {

            }

        }



        super.onActivityResult(requestCode, resultCode, data)
    }



}