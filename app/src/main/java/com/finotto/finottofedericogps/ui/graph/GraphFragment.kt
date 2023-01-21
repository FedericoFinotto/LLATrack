package com.finotto.finottofedericogps.ui.graph

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.finotto.finottofedericogps.R
import java.util.*
import android.graphics.Color
import android.graphics.Typeface
import androidx.fragment.app.Fragment
import com.finotto.finottofedericogps.database.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class GraphFragment : Fragment() {

    private lateinit var inflatedView : View
    private lateinit var grafico_Lat : LineChart
    private lateinit var grafico_Long : LineChart
    private lateinit var grafico_Alt : LineChart
    private lateinit var repo: Database

    private val valoriLatitudine = arrayListOf<Entry>()
    private val valoriLongitudine = arrayListOf<Entry>()
    private val valoriAltitudine = arrayListOf<Entry>()

    override fun onCreateView(
        inflater : LayoutInflater, container : ViewGroup?,
        savedInstanceState : Bundle?
    ) : View {
        repo = (requireActivity().application as DatabaseApplication).database
        inflatedView = inflater.inflate(R.layout.fragment_graph, container, false)

        grafico_Long = inflatedView.findViewById(R.id.graficoAltitudine)
        grafico_Lat = inflatedView.findViewById(R.id.graficoLongitudine)
        grafico_Alt = inflatedView.findViewById(R.id.graficoLatitudine)


        inizializzaGrafico(grafico_Alt)
        inizializzaGrafico(grafico_Long)
        inizializzaGrafico(grafico_Lat)

        return inflatedView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var listaValori = repo.listSample;
        valoriAltitudine.clear()
        valoriLongitudine.clear()
        valoriLatitudine.clear()


        for((index, location) in listaValori.reversed().withIndex()){
            valoriAltitudine.add(Entry(index.toFloat() + 1, location.latitudine.toFloat()))
            valoriLatitudine.add(Entry(index.toFloat() + 1, location.longitudine.toFloat()))
            valoriLongitudine.add(Entry(index.toFloat() + 1, location.altitudine.toFloat()))
        }

        aggiornaGrafico(grafico_Lat, valoriAltitudine, "Latitudine", Color.RED)
        aggiornaGrafico(grafico_Long, valoriLatitudine, "Longitudine", Color.GREEN)
        aggiornaGrafico(grafico_Alt, valoriLongitudine, "Altitudine", Color.BLUE)
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

    private fun inizializzaGrafico(chart : LineChart){

        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.typeface = Typeface.DEFAULT_BOLD
        chart.xAxis.textColor = Color.WHITE

        // Y axis settings
        chart.axisLeft.textColor = Color.WHITE
        chart.axisRight.isEnabled = false
        chart.legend.textColor = Color.WHITE
        chart.legend.textSize = 20f
        chart.setNoDataText("Nessun Dato")
    }

}