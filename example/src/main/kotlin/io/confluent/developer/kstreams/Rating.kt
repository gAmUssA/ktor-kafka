package io.confluent.developer.kstreams

/**
 * Rating class for storing a movie rating
 */
data class Rating(val movieId: Long = 1L, val rating: Double = 0.0)

/**
 * intermediate holder of CountAndSum
 */
data class CountAndSum(var count: Long = 0L, var sum: Double = 0.0)
