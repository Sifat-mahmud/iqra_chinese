import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:iqra_chinese/Archieve/wordModel.dart';

class WordListController extends GetxController {
  RxList<WordModel> wordList = <WordModel>[
    WordModel(
        character: '我',
        pinyin: 'wǒ',
        meaning: 'I, me',
        examplePinyin: '',
        exampleMeaning: '',
        hskLevel: 1),

  ].obs;



  @override
  void onInit() {
    super.onInit();
    // wordList.clear();
    loadWordList();
  }



  loadWordList() async {
    // Load the JSON file
    String jsonString = await rootBundle.loadString('lib/Archieve/hsk2.json'); // Correct path to your JSON file

    // Decode the JSON data
    final List<dynamic> jsonResponse = json.decode(jsonString);

    // Map JSON data to a list of WordModel
    wordList = RxList<WordModel>.from(
        jsonResponse.map((model) => WordModel.fromJson(model)).toList()
    );

    // return wordList;
  }

}
