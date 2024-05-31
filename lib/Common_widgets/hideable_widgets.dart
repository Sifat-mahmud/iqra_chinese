import 'package:flutter/material.dart';

import '../main.dart';

class HidableWidget extends StatefulWidget {
  final String text;
  final String textHeader;
  final bool initiallyVisible;
  final bool alignCentre;

  HidableWidget({
    Key? key,
    required this.text,
    required this.textHeader,
    this.initiallyVisible = true,
    this.alignCentre = true,
  }) : super(key: key);

  @override
  _HidableWidgetState createState() => _HidableWidgetState();
}

class _HidableWidgetState extends State<HidableWidget> {
  late bool isVisible;

  @override
  void initState() {
    super.initState();
    isVisible = widget.initiallyVisible;
  }

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisAlignment: widget.alignCentre ? MainAxisAlignment.center : MainAxisAlignment.start,
      children: [
        Text(
          widget.textHeader,
          style: TextStyle(fontSize: 16.0),
        ),
        if (isVisible)
          GestureDetector(
            onTap: () {
              ttsServiceEn.speak(widget.text);
            },
            child: Text(
              widget.text,
              style: TextStyle(fontSize: 16.0),
            ),
          ),
        IconButton(
          onPressed: () {
            setState(() {
              isVisible = !isVisible;
            });
          },
          icon: Icon(isVisible ? Icons.visibility_off : Icons.visibility),
        ),
      ],
    );
  }
}
