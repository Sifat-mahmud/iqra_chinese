import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:iqra_chinese/Archieve/wordList_controller.dart';
import 'package:iqra_chinese/Archieve/wordModel.dart';
import 'package:iqra_chinese/Common_View/word_view.dart';

import '../main.dart';

class WordViewBuilder extends StatefulWidget {
  const WordViewBuilder({super.key});

  @override
  State<WordViewBuilder> createState() => _WordViewBuilderState();
}

class _WordViewBuilderState extends State<WordViewBuilder> {
  final WordListController wordListController = Get.find();
  int currentIndex = 1;

  late WordModel word ;
  void _incrementIndex() {
    setState(() {
      if (currentIndex < wordListController.wordList.length - 1) {
        currentIndex++;
      }
    });

    word = wordListController.wordList[currentIndex];
    playCharacter();


  }

  void _decrementIndex() {
    setState(() {
      if (currentIndex > 0) {
        currentIndex--;
      }
    });
    word = wordListController.wordList[currentIndex];
    playCharacter();

  }

  @override
  Widget build(BuildContext context) {
    word = wordListController.wordList[currentIndex];
    return Scaffold(
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          children: [

            Expanded(
              child: WordView(word: word ),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                ElevatedButton(
                  onPressed: _decrementIndex,
                  child: Icon(Icons.arrow_left),
                ),
                ElevatedButton(
                  onPressed: () {
                    playCharacter();
                  },
                  child: Icon(Icons.volume_up_sharp),
                ),
                ElevatedButton(
                  onPressed: _incrementIndex,
                  child: Icon(Icons.arrow_right),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void playCharacter() {

    ttsServiceCn.speak(word.character);
  }
}
