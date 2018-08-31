import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.nio.file.Path

object Git {
    fun cloneRepository(repoUrl: String, tempDir: Path, login: String?, password : String?) {
        Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(tempDir.toFile())
                .also {
                    if (login != null) {
                        val pass = password
                                ?: System.console()
                                        .readPassword("Enter password for github account $login: ")
                                        ?.joinToString("")
                        it.setCredentialsProvider(UsernamePasswordCredentialsProvider(login, pass))
                    }
                }
                .setProgressMonitor(object : ProgressMonitor {
                    override fun update(completed: Int) {}
                    override fun start(totalTasks: Int) = println("Start")
                    override fun beginTask(title: String?, totalWork: Int) = println("$title $totalWork")
                    override fun endTask() = println("Done!")
                    override fun isCancelled() = false
                })
                .call()
                .close()
    }
}