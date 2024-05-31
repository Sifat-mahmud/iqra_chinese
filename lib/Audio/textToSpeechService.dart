import 'package:flutter_tts/flutter_tts.dart';

class EnTextToSpeechService {
  FlutterTts flutterTts = FlutterTts();

  EnTextToSpeechService() {
    initializeTts();
  }

  Future<void> initializeTts() async {
    await flutterTts.setLanguage('en-US'); // Set desired language
    await flutterTts.setPitch(1.0); // Set pitch (0.0 to 1.0)
    await flutterTts.setSpeechRate(0.5); // Set speech rate (0.0 to 1.0)
  }

  Future<void> speak(String text) async {
    await flutterTts.speak(text);
  }
}


class CnTextToSpeechService {
  FlutterTts flutterTts = FlutterTts();

  TextToSpeechService() {
    initializeTts();
  }

  Future<void> initializeTts() async {
    await flutterTts.setLanguage('zh-CN');
    await flutterTts.setPitch(1.0); // Set pitch (0.0 to 1.0)
    await flutterTts.setSpeechRate(0.6); // Set speech rate (0.0 to 1.0)
  }

  Future<void> speak(String text) async {
    await flutterTts.speak(text);
  }
}