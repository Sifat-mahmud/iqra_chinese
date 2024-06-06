import 'dart:convert';
import 'package:flutter/services.dart' show rootBundle;

class WordModel {
  final String character;
  final String pinyin;
  final String meaning;
  final String examplePinyin;
  final String exampleMeaning;
  final int hskLevel;

  WordModel({
    required this.character,
    required this.pinyin,
    required this.meaning,
    this.examplePinyin = '',
    this.exampleMeaning = '',
    required this.hskLevel,
  });

  factory WordModel.fromJson(Map<String, dynamic> json) {
    return WordModel(
      character: json['character'],
      pinyin: json['pyning'], // Note: 'pyning' should be 'pinyin' in your JSON
      meaning: json['meaning'],
      examplePinyin: json['examplePinyin'] ?? '',
      exampleMeaning: json['exampleMeaning'] ?? '',
      hskLevel: json['hskLevel'] ?? 1, // Default to HSK Level 1 if not provided
    );
  }
}