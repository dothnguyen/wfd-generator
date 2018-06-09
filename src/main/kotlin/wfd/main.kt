package com.nguyendo.wfd

import com.google.cloud.texttospeech.v1beta1.*
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


/**
 * Created by voiu on 6/6/18.
 *
 */

val VOICE_NAMES = listOf<String>("en-US-Wavenet-D",
        "en-US-Wavenet-A",
        "en-US-Wavenet-B",
        "en-US-Wavenet-C",
        "en-US-Wavenet-E",
        "en-US-Wavenet-F")

val VOICE_GENDERS = listOf<SsmlVoiceGender>(SsmlVoiceGender.MALE, SsmlVoiceGender.FEMALE)

val SSML_PRE = "<s><break time=\"2000ms\"></break>"
val SSML_POST = "</s>"

fun main(args: Array<String>) {
    //synthesizeText("Many students are so scared of writing essays, because they never learned how.")

    val outputFolder = "output1/"

    File(outputFolder).mkdir()

    // Instantiates a client
    val textToSpeechClient = TextToSpeechClient.create()

    // Select the type of audio file you want returned
    val audioConfig = AudioConfig.newBuilder()
            .setAudioEncoding(AudioEncoding.MP3) // MP3 audio.
            .setSpeakingRate(1.26)
            .build()

    val sentences = Files.lines(Paths.get(ClassLoader.getSystemClassLoader().getResource("sentences.txt").toURI()))
    var idx = 1
    sentences.forEach {sentence ->

        synthesizeText(textToSpeechClient,
                audioConfig,
                SSML_PRE + sentence + SSML_POST,
                "$outputFolder${idx.toString().padStart(3, '0')}.mp3",
                randVoice(),
                randGender())

        Thread.sleep(500)

        idx++
    }
}

/**
 * generate a voice
 */
fun randVoice(): String {
    val random = Random().nextInt(VOICE_NAMES.size)
    return VOICE_NAMES[random]
}

/**
 * random a gender
 */
fun randGender(): SsmlVoiceGender {
    val random = Random().nextInt(VOICE_GENDERS.size)
    return VOICE_GENDERS[random]
}

@Throws(Exception::class)
fun synthesizeText(textToSpeechClient:TextToSpeechClient,
                   audioConfig: AudioConfig,
                   text: String,
                   outputFile: String,
                   voiceName: String,
                   voiceGender: SsmlVoiceGender) {

    // Set the text input to be synthesized
    val input = SynthesisInput.newBuilder()
            //.setText(text)
            .setSsml(text)
            .build()

    // Build the voice request
    val voice = VoiceSelectionParams.newBuilder()
            .setLanguageCode("en-US") // languageCode = "en_us"
            .setName(voiceName/*"en-US-Wavenet-D"*/)
            .setSsmlGender(voiceGender) // ssmlVoiceGender = SsmlVoiceGender.FEMALE
            .build()

    // Perform the text-to-speech request
    val response = textToSpeechClient.synthesizeSpeech(input, voice,
            audioConfig)

    // Get the audio contents from the response
    val audioContents = response.audioContent

    // Write the response to the output file.
    FileOutputStream(outputFile).use { out ->
        out.write(audioContents.toByteArray())
        // wait 500 millis before making another request.
        println("Audio content written to file \"$outputFile\"")
    }
}

/**
 *
 */
data class WFDQuestion(val text: String, val mp3File: String)