class ConsoleGPT {
    fun startConsoleGPT() {
        val openai = OpenaiAPI()
        val response = openai.chatGPT("hello")
        println(response)
    }
}