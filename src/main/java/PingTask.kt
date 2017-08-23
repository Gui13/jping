import java.net.InetAddress
import java.util.concurrent.atomic.AtomicBoolean
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket




class PingTask(private val host: String, private val delayMs: Int = 1000) : Runnable {

    private val stop: AtomicBoolean = AtomicBoolean(false)
    private val inetAddr = InetAddress.getByName(host)
    private val listeners: MutableList<PinkTaskListener> = mutableListOf()

    constructor(host: String, listener: PinkTaskListener, delayMs: Int = 1000) : this(host, delayMs) {
        addListener(listener)
    }

    override fun run() {
        while (!stop.get()) {
            val start = System.nanoTime()
            val reachable = isReachable(host, timeOutMillis = delayMs) //inetAddr.isReachable(delayMs)
            val duration : Double = (System.nanoTime() - start).toDouble() / 1000_000

            listeners.forEach {
                it.ping_received(host, if (reachable) duration else (-1).toDouble())
            }

            if (duration < delayMs) {
                Thread.sleep(delayMs - duration.toLong() )
            }
        }
    }

    fun addListener(listener: PinkTaskListener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    private fun isReachable(addr: String, openPort: Int = 80, timeOutMillis: Int): Boolean {
        // Any Open port on other machine
        // openPort =  22 - ssh, 80 or 443 - webserver, 25 - mailserver etc.
        return try {
            Socket().use { soc -> soc.connect(InetSocketAddress(addr, openPort), timeOutMillis) }
            true
        } catch (ex: IOException) {
            false
        }

    }

    fun removeListener(listener: PinkTaskListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    fun stop() {
        stop.set(true)
    }

}