import 'package:flutter/material.dart';

class ListIndexView extends StatefulWidget {
  final int index;
  final int ListLen;

  ListIndexView({required this.index, required this.ListLen});

  @override
  _ListIndexViewState createState() => _ListIndexViewState();
}

class _ListIndexViewState extends State<ListIndexView> {


  @override
  Widget build(BuildContext context) {
    return Container(
      child: Text(
        '${widget.index+1}/${widget.ListLen}',
        style: TextStyle(fontSize: 15,),
      ),
    );
  }
}