import { ref, onUnmounted } from 'vue'

export function useSpeech() {
  const isListening = ref(false)
  const transcript = ref('')
  const isSpeaking = ref(false)

  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  // Speech input and output supported independently
  const isRecognitionSupported = !!SpeechRecognition
  const isSynthSupported = !!synth
  const isSupported = isRecognitionSupported || isSynthSupported

  let recognition = null
  let synth = window.speechSynthesis || null

  function startListening() {
    if (!SpeechRecognition) return
    if (recognition) {
      recognition.abort()
    }
    recognition = new SpeechRecognition()
    recognition.lang = 'zh-CN'
    recognition.interimResults = true
    recognition.continuous = false
    recognition.maxAlternatives = 1

    recognition.onstart = () => {
      isListening.value = true
      transcript.value = ''
    }

    recognition.onresult = (event) => {
      let result = ''
      for (let i = 0; i < event.results.length; i++) {
        result += event.results[i][0].transcript
      }
      transcript.value = result
    }

    recognition.onerror = (event) => {
      console.warn('Speech recognition error:', event.error)
      isListening.value = false
    }

    recognition.onend = () => {
      isListening.value = false
    }

    try {
      recognition.start()
    } catch (e) {
      console.warn('Speech recognition start failed:', e)
      isListening.value = false
    }
  }

  function stopListening() {
    if (recognition) {
      try {
        recognition.stop()
      } catch (e) {
        // ignore
      }
      isListening.value = false
    }
  }

  // Split long text into chunks to avoid Chrome's ~15s speech cutoff bug
  function splitText(text, maxLen = 200) {
    const chunks = []
    const sentences = text.split(/(?<=[。！？；\n.!?;])/)
    let current = ''
    for (const sentence of sentences) {
      if ((current + sentence).length > maxLen && current) {
        chunks.push(current.trim())
        current = ''
      }
      current += sentence
    }
    if (current.trim()) {
      chunks.push(current.trim())
    }
    if (chunks.length === 0 && text.length > 0) {
      for (let i = 0; i < text.length; i += maxLen) {
        chunks.push(text.slice(i, i + maxLen))
      }
    }
    return chunks
  }

  function speak(text) {
    if (!synth) return
    stopSpeaking()

    const chunks = splitText(text)
    let index = 0

    function speakNext() {
      if (index >= chunks.length) {
        isSpeaking.value = false
        return
      }
      const utterance = new SpeechSynthesisUtterance(chunks[index])
      utterance.lang = 'zh-CN'
      utterance.rate = 1.0
      utterance.pitch = 1.0

      const voices = synth.getVoices()
      const zhVoice = voices.find(v => v.lang.startsWith('zh'))
      if (zhVoice) {
        utterance.voice = zhVoice
      }

      utterance.onend = () => {
        index++
        speakNext()
      }
      utterance.onerror = () => {
        isSpeaking.value = false
      }

      synth.speak(utterance)
    }

    isSpeaking.value = true
    speakNext()
  }

  function stopSpeaking() {
    if (synth) {
      synth.cancel()
      isSpeaking.value = false
    }
  }

  onUnmounted(() => {
    stopListening()
    stopSpeaking()
  })

  return {
    isListening,
    transcript,
    isSpeaking,
    isSupported,
    isRecognitionSupported,
    isSynthSupported,
    startListening,
    stopListening,
    speak,
    stopSpeaking
  }
}
