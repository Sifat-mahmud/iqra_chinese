import 'package:flutter/material.dart';

class WrappedText extends StatelessWidget {
  final String text;
  final double ratio;
  final TextStyle? textStyle;

  WrappedText({required this.text, required this.ratio , this.textStyle});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: MediaQuery.of(context).size.width * ratio,
      child: Center(
        child: Text(
          text,
          style: textStyle ?? TextStyle(fontSize: 25.0),
          maxLines: null, // Allows text to wrap to the next line indefinitely
          overflow: TextOverflow.visible, // Text will not be clipped or show ellipsis
        ),
      ),
    );
  }
}
