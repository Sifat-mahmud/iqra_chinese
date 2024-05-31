import 'package:flutter/material.dart';
import 'package:get/get.dart';

import '../Archieve/wordModel.dart';
import '../Common_widgets/hideable_widgets.dart';
import '../main.dart';

class SentenceView extends StatefulWidget {
  final WordModel sentence;
  final bool meaningVisible;
  final bool pinyinVisible;

  SentenceView({
    Key? key,
    required this.sentence,
    this.meaningVisible = true,
    this.pinyinVisible = true,
  }) : super(key: key);

  @override
  _SentenceViewState createState() => _SentenceViewState();
}

class _SentenceViewState extends State<SentenceView> {
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
    return Column(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        Center(
          child: GestureDetector(
            onTap: () {
              ttsServiceCn.speak(widget.sentence.character);
            },
            child: Text(
              widget.sentence.character,
              style: TextStyle(fontSize: 60.0, fontWeight: FontWeight.bold),
            ),
          ),
        ),
        Center(
          child: HidableWidget(
            textHeader: 'Meaning: ',
            text: '${widget.sentence.meaning}',
            initiallyVisible: meaningVisible,
          ),
        ),
        Center(
          child: HidableWidget(
            textHeader: 'Pinyin: ',
            text: '${widget.sentence.pinyin}',
            initiallyVisible: pinyinVisible,
          ),
        ),
      ],
    );
  }
}
