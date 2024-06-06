import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:iqra_chinese/Archieve/wordList_controller.dart';
import 'package:iqra_chinese/Archieve/wordModel.dart';
import 'package:iqra_chinese/Common_View/word_view.dart';

import '../Common_widgets/listIndex_view.dart';
import '../Common_widgets/searchBar.dart';
import '../main.dart';

class WordViewBuilder extends StatefulWidget {
  const WordViewBuilder({super.key});

  @override
  State<WordViewBuilder> createState() => _WordViewBuilderState();
}

class _WordViewBuilderState extends State<WordViewBuilder> {
  final WordListController wordListController = Get.find();
  int currentIndex = 0;

  late int wordListLen ;

  late WordModel? word ;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    wordListLen = wordListController.wordList.length;
  }


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
    return word ==null ?
       CircularProgressIndicator()
        :
    Padding(
      padding: const EdgeInsets.all(8.0),
      child: Scaffold(
        bottomNavigationBar: Row(
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
        body: SingleChildScrollView(
          child: Container(
            height: MediaQuery.of(context).size.height,
            child: Column(

              children: [

                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    ListIndexView(
                      index: currentIndex,
                      ListLen: wordListController.wordList.length,
                    ),

                    SearchBox(
                      items: wordListController.wordList,
                      onItemSelected: (index) {

                        // print('Selected index: $index');
                        setState(() {
                          currentIndex = index;

                        });
                      },
                    ),


                  ],
                ),

                SizedBox(
                  height: 15,
                ),
                WordView(word: word! ),


              ],
            ),
          ),
        ),
      ),
    );
  }

  Future<void> playCharacter() async {

    await ttsServiceCn.speak('${word?.character ?? "Missing character"}${"       "}${word?.meaning ?? "No meaning"}');
    // sleep( Duration(seconds: 10));
    // ttsServiceEn.speak(word?.meaning ?? "No meaning");
  }
}
