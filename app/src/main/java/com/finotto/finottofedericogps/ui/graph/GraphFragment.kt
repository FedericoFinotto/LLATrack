package com.finotto.finottofedericogps.ui.graph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.finotto.finottofedericogps.R
import java.util.*
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.util.toRange
import androidx.fragment.app.Fragment
import com.finotto.finottofedericogps.database.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class GraphFragment : Fragment() {

    private val tempoReale = true
    private lateinit var inflatedView : View
    private lateinit var grafico_Lat : LineChart
    private lateinit var grafico_Long : LineChart
    private lateinit var grafico_Alt : LineChart
    private lateinit var db: Database
    private lateinit var handler: Handler

    private val valoriLatitudine = arrayListOf<Entry>()
    private val valoriLongitudine = arrayListOf<Entry>()
    private val valoriAltitudine = arrayListOf<Entry>()

    override fun onCreateView(
        inflater : LayoutInflater, container : ViewGroup?,
        savedInstanceState : Bundle?
    ) : View {
        db = (requireActivity().application as DatabaseApplication).database
        inflatedView = inflater.inflate(R.layout.fragment_graph, container, false)

        grafico_Lat = inflatedView.findViewById(R.id.graficoLatitudine)
        grafico_Long = inflatedView.findViewById(R.id.graficoLongitudine)
        grafico_Alt = inflatedView.findViewById(R.id.graficoAltitudine)

        inizializzaGrafico(grafico_Alt,"altitudine")
        inizializzaGrafico(grafico_Long,"longitudine")
        inizializzaGrafico(grafico_Lat,"latitudine")

        return inflatedView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listaValori = db.listSample
        valoriAltitudine.clear()
        valoriLongitudine.clear()
        valoriLatitudine.clear()

        val questoIstante = Date()
        listaValori.reversed().forEach {
            val distanza = questoIstante.getTime() - it.timestamp.getTime()
            if (distanza <= (5 * 60 * 1000)) {
                val minuti = -distanza.toFloat()/60/1000
                valoriAltitudine.add(Entry(minuti, it.altitudine.toFloat()))
                valoriLatitudine.add(Entry(minuti, it.latitudine.toFloat()))
                valoriLongitudine.add(Entry(minuti, it.longitudine.toFloat()))
            }
        }
        aggiornaGrafici.aggiorna()
    }

    private val aggiornaGrafici = object : Runnable{
        override fun run(){
            aggiorna()
            handler.postDelayed(this, "10".toLong())
        }

        fun aggiorna(){
            aggiornaGrafico(grafico_Lat, valoriLatitudine, getString(R.string.text_Latitude), Color.RED)
            aggiornaGrafico(grafico_Long, valoriLongitudine, getString(R.string.text_Longitude), Color.GREEN)
            aggiornaGrafico(grafico_Alt, valoriAltitudine, getString(R.string.text_Altitude), Color.BLUE)
        }
    }

    private fun definisciLineeDataSet(listaValori : ArrayList<Entry>, titolo : String, colore : Int) : LineDataSet{
        val grafico = LineDataSet(listaValori, titolo)
        grafico.color = colore //Imposto il valore della linea tracciata
        grafico.highLightColor = Color.TRANSPARENT //Rimuovo l'Highlight
        grafico.setDrawCircles(false) //Rimuove i punti per ogni valore registrato
        grafico.lineWidth = 2f
        return grafico
    }

    private fun aggiornaGrafico(chart : LineChart, values : ArrayList<Entry>, valueLabel : String, valueColor : Int){
        if(chart.data != null && chart.data.dataSetCount > 0){
            val set = chart.data.getDataSetByIndex(0) as LineDataSet
            set.values = values
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
            return
        }

        val lineData = LineData(definisciLineeDataSet(values, valueLabel, valueColor))
        lineData.setDrawValues(false)
        chart.data = lineData

        chart.data.notifyDataChanged()
        chart.notifyDataSetChanged()
        chart.invalidate()
        chart.fitScreen()
    }

    private fun inizializzaGrafico(chart : LineChart, coord: String){
        chart.xAxis.apply{
            position = XAxis.XAxisPosition.BOTTOM
            typeface = Typeface.DEFAULT_BOLD
            textColor = Color.WHITE
            granularity = 1f
            isGranularityEnabled = true
            axisMaximum = 0f
            axisMinimum = -5f
        }

        chart.axisRight.apply{
            maxWidth = 0.toFloat()
            isEnabled = false
        }

        chart.axisLeft.apply{
            textColor = Color.WHITE
            isEnabled = true
            minWidth = 55.toFloat()

            val range = 0.0001f
            if(coord=="latitudine" || coord=="longitudine"){
                axisMaximum = db.massimo(coord).toFloat()+range
                axisMinimum = db.minimo(coord).toFloat()-range
            }
        }

        chart.legend.apply{
            textColor = Color.WHITE
            textSize = 20f
        }

        chart.setNoDataText("Nessun Dato")
    }

}