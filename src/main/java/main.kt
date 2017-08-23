import java.net.InetAddress

class PingListener : PinkTaskListener {
    override fun ping_received(host: String, timeMs: Double) {
        println(if (timeMs > 0) "$host reached in $timeMs" else "$host unreachable")
    }

}


fun main(args: Array<String>) {

    val threads = mutableListOf<Thread>()
    val tasks = mutableListOf<PingTask>()

    args.forEach {
        val pingTask = PingTask(it, PingListener())
        threads.add(Thread(pingTask))
        tasks.add(pingTask)
    }

    threads.forEach { it.start() }

    Thread.sleep(5_000)
    println("Exiting")

    tasks.forEach { it.stop() }
    threads.forEach { it.join() }

    println(if (InetAddress.getByName("google.fr").isReachable(1000)) "YES" else "NO")

}