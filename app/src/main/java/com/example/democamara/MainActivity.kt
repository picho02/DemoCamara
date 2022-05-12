package com.example.democamara

import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.camera.camera2.internal.compat.workaround.MaxPreviewSize
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.democamara.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var outPutDirectory:File
    private var imageCapture: ImageCapture ?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        outPutDirectory = getOutPutDirectory()
        requestPermission()
        binding.btnTomarFoto.setOnClickListener {
            takePhoto()
        }
    }

    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                mPreview -> mPreview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            imageCapture=ImageCapture.Builder().build()
            val cameraSelector=CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,cameraSelector,preview,imageCapture
                )
            }catch (e:Exception){
                Log.d(Constants.TAG,"Error al iniciar la camara",e)
            }
        },ContextCompat.getMainExecutor(this))
    }
    private fun takePhoto(){
        val imageCapture = imageCapture?:return
        val photoFile = File(
            outPutDirectory,SimpleDateFormat(Constants.FILE_NAME_FORMAT,Locale.getDefault()).format(System.currentTimeMillis())+".jpg")
        val outPutOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(outPutOptions,ContextCompat.getMainExecutor(this),object:
            ImageCapture.OnImageSavedCallback {
            override fun onImageSaved (outputFielResult: ImageCapture.OutputFileResults){
                val savedUri = Uri.fromFile(photoFile)
                val msg= "foto guardad"
                Log.i(Constants.TAG,"Foto: $msg, $savedUri")
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(Constants.TAG,"Error")
            }
        }
        )
    }

    private fun getOutPutDirectory():File{
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            mFile-> File(mFile,"democlasecamara").apply {
                mkdirs()
        }
        }
        return if(mediaDir!=null && mediaDir.exists())
            mediaDir else filesDir
    }
    private fun requestPermission(){
        if(allPermissionGranted()){
            startCamera()
        }else{
            ActivityCompat.requestPermissions(this,Constants.REQUIRED_PERMISSION,Constants.REQUEST_CODE_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode==Constants.REQUEST_CODE_PERMISSION){
            if (allPermissionGranted()){
                //inicializar la camara
                startCamera()
            }else{
                finish()
            }
        }
    }
    private fun allPermissionGranted()=
        Constants.REQUIRED_PERMISSION.all {
            ContextCompat.checkSelfPermission(baseContext,it) == PackageManager.PERMISSION_GRANTED

    }
}