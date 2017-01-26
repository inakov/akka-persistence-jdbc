package cache

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap

/**
  * Created by inakov on 26.01.17.
  */
trait CacheWrapper[K, V] {

  def get(key: K): Option[V]

  def put(key: K, value: V): Unit

}

class SimpleLurCacheWrapper[K, V](val initCapacity: Int, val maxCapacity: Int) extends CacheWrapper[K, V]{

  val cache = new ConcurrentLinkedHashMap.Builder[K, V]
    .initialCapacity(initCapacity)
    .maximumWeightedCapacity(maxCapacity)
    .build()

  override def get(key: K): Option[V] = Option(cache.get(key))

  override def put(key: K, value: V): Unit = cache.put(key, value)

}
