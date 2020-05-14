package io.horizontalsystems.chartview

import android.graphics.RectF
import io.horizontalsystems.chartview.ChartCurve.Coordinate
import io.horizontalsystems.chartview.models.ChartConfig
import io.horizontalsystems.chartview.models.ChartPoint

class ChartHelper(private val shape: RectF, private val config: ChartConfig) {

    fun setCoordinates(points: List<ChartPoint>, startTimestamp: Long, endTimestamp: Long): List<Coordinate> {
        val width = shape.width()
        val height = shape.height()

        val deltaX = (endTimestamp - startTimestamp) / width
        val deltaY = (config.valueTop - config.valueLow) / height

        return points.map { point ->
            val x = (point.timestamp - startTimestamp) / deltaX
            val y = (point.value - config.valueLow) / deltaY

            Coordinate(x, height - y, point)
        }
    }

    fun getTopAndLow(coordinates: List<Coordinate>): Pair<Coordinate, Coordinate> {
        var topCoordinate = coordinates[0]
        var lowCoordinate = coordinates[0]

        for (coordinate in coordinates) {
            if (coordinate.point.value > topCoordinate.point.value) {
                topCoordinate = coordinate
            }

            if (coordinate.point.value < lowCoordinate.point.value) {
                lowCoordinate = coordinate
            }
        }

        return Pair(topCoordinate, lowCoordinate)
    }

}
