class WordModel {
  String character;
  String pinyin;
  String meaning;
  String examplePinyin ;
  String exampleMeaning;
  int hskLevel;

  WordModel({
    required this.character,
    required this.pinyin,
    required this.meaning,
    this.examplePinyin = "",
    this.exampleMeaning = "",
    required this.hskLevel,
  });

  factory WordModel.fromJson(Map<String, dynamic> json) {
    return WordModel(
      character: json['character'],
      pinyin: json['pinyin'],
      meaning: json['meaning'],
      examplePinyin: json['examplePinyin'],
      exampleMeaning: json['exampleMeaning'],
      hskLevel: json['hskLevel'],
    );
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['character'] = this.character;
    data['pinyin'] = this.pinyin;
    data['meaning'] = this.meaning;
    data['examplePinyin'] = this.examplePinyin;
    data['exampleMeaning'] = this.exampleMeaning;
    data['hskLevel'] = this.hskLevel;
    return data;
  }
}