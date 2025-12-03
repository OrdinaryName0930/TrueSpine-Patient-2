package com.brightcare.patient.ui.component.complete_your_profile

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException

data class AddressData(
    val provinces: List<String> = emptyList(),
    val municipalities: Map<String, List<String>> = emptyMap(),
    val barangays: Map<String, List<String>> = emptyMap()
)

@Composable
fun rememberAddressData(): AddressData {
    val context = LocalContext.current
    
    // Use remember with context as key to prevent unnecessary reloading
    // and use a stable initial state
    return remember(context) {
        // Create a stable state holder
        mutableStateOf(AddressData())
    }.apply {
        // Load data only once
        LaunchedEffect(context) {
            if (value.provinces.isEmpty()) {
                value = loadAddressData(context)
            }
        }
    }.value
}

@Composable
fun rememberAddressDataOnce(): AddressData? {
    val context = LocalContext.current
    // produceState loads once on composition and keeps same instance
    val addressDataState = produceState<AddressData?>(initialValue = null, context) {
        value = loadAddressData(context)
    }
    return addressDataState.value
}

private suspend fun loadAddressData(context: Context): AddressData = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.assets.open("address/address.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        
        val provinces = mutableListOf<String>()
        val municipalities = mutableMapOf<String, List<String>>()
        val barangays = mutableMapOf<String, List<String>>()
        
        // Parse JSON structure
        val regions = jsonObject.keys()
        while (regions.hasNext()) {
            val regionKey = regions.next()
            val regionObject = jsonObject.getJSONObject(regionKey)
            val provinceList = regionObject.getJSONObject("province_list")
            
            val provinceKeys = provinceList.keys()
            while (provinceKeys.hasNext()) {
                val provinceName = provinceKeys.next()
                provinces.add(provinceName)
                
                val provinceObject = provinceList.getJSONObject(provinceName)
                val municipalityList = provinceObject.getJSONObject("municipality_list")
                
                val municipalityNames = mutableListOf<String>()
                val municipalityKeys = municipalityList.keys()
                
                while (municipalityKeys.hasNext()) {
                    val municipalityName = municipalityKeys.next()
                    municipalityNames.add(municipalityName)
                    
                    val municipalityObject = municipalityList.getJSONObject(municipalityName)
                    val barangayArray = municipalityObject.getJSONArray("barangay_list")
                    
                    val barangayNames = mutableListOf<String>()
                    for (i in 0 until barangayArray.length()) {
                        barangayNames.add(barangayArray.getString(i))
                    }
                    
                    barangays["$provinceName-$municipalityName"] = barangayNames
                }
                
                municipalities[provinceName] = municipalityNames
            }
        }
        
        AddressData(
            provinces = provinces.sorted(),
            municipalities = municipalities,
            barangays = barangays
        )
    } catch (e: IOException) {
        e.printStackTrace()
        AddressData()
    } catch (e: Exception) {
        e.printStackTrace()
        AddressData()
    }
}
