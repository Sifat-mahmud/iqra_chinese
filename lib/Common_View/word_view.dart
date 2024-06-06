import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:iqra_chinese/Common_widgets/wrapped_text.dart';

import '../Archieve/wordModel.dart';
import '../Common_widgets/hideable_widgets.dart';
import '../Common_widgets/listIndex_view.dart';
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

          blank(),

          blank(),

          GestureDetector(
            onTap: () {
              ttsServiceCn.speak(widget.word.character);
            },
            child: WrappedText(
              text : widget.word.character,
              ratio: .90,
              textStyle: TextStyle(fontSize: 60.0, fontWeight: FontWeight.bold),
            ),
          ),

          blank(),

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

          blank(),
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

  Widget blank(){
    return SizedBox(height: 8,);
  }
}
