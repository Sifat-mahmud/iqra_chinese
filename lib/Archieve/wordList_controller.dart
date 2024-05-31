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
    WordModel(
        character: '我们',
        pinyin: 'wǒmen',
        meaning: 'we, us',
        examplePinyin: '',
        exampleMeaning: '',
        hskLevel: 1),
    WordModel(
        character: '你',
        pinyin: 'nǐ',
        meaning: 'you',
        examplePinyin: '',
        exampleMeaning: '',
        hskLevel: 1),
    WordModel(
        character: '他',
        pinyin: 'tā',
        meaning: 'he, him',
        examplePinyin: '',
        exampleMeaning: '',
        hskLevel: 1),
    WordModel(
        character: '她',
        pinyin: 'tā',
        meaning: 'she, her',
        examplePinyin: '',
        exampleMeaning: '',
        hskLevel: 1),
  ].obs;
}
