import 'package:flutter/material.dart';
import 'package:get/get.dart';

import '../Archieve/wordModel.dart';
import '../Common_widgets/hideable_widgets.dart';
import '../main.dart';

class WordView extends StatefulWidget {
  final WordModel word;
  final bool meaningVisible;
  final bool pinyinVisible;

  WordView({
    Key? key,
    required this.word,
    this.meaningVisible = true,
    this.pinyinVisible = true,
  }) : super(key: key);

  @override
  _WordViewState createState() => _WordViewState();
}

class _WordViewState extends State<WordView> {
  late bool meaningVisible;
  late bool pinyinVisible;

  @override
  void initState() {
    super.initState();
    meaningVisible = widget.meaningVisible;
    pinyinVisible = widget.pinyinVisible;
  }

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          Center(
            child: GestureDetector(
              onTap: () {
                ttsServiceCn.speak(widget.word.character);
              },
              child: Text(
                widget.word.character,
                style: TextStyle(fontSize: 60.0, fontWeight: FontWeight.bold),
              ),
            ),
          ),
          // if (meaningVisible)
          //   Center(
          //     child: GestureDetector(
          //       onTap: () {
          //         ttsServiceEn.speak(widget.word.meaning);
          //       },
          //       child: Text(
          //         'Meaning: ${widget.word.meaning}',
          //         style: TextStyle(fontSize: 16.0),
          //       ),
          //     ),
          //   ),
          Center(
            child: HidableWidget(
              textHeader: 'Meaning: ',
              text: '${widget.word.meaning}',
              initiallyVisible: meaningVisible,
            ),
          ),
          Center(
            child: HidableWidget(
              textHeader: 'Pinyin: ',
              text: '${widget.word.pinyin}',
              initiallyVisible: pinyinVisible,
            ),
          ),
        ],
      ),
    );
  }
}
