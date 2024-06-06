import 'package:flutter/material.dart';

import '../Archieve/wordModel.dart';

class SearchBox extends StatefulWidget {
  final List<WordModel> items;
  final ValueChanged<int> onItemSelected;

  SearchBox({required this.items, required this.onItemSelected});

  @override
  _SearchBoxState createState() => _SearchBoxState();
}

class _SearchBoxState extends State<SearchBox> {
  TextEditingController _controller = TextEditingController();
  List<WordModel> _filteredItems = [];
  bool _isTyping = false; // Flag to track if someone is typing

  @override
  void initState() {
    super.initState();
    _filteredItems = widget.items;
  }

  void _filterItems(String query) {
    setState(() {
      _isTyping = query.isNotEmpty; // Update flag based on text input
      _filteredItems = widget.items.where((item) {
        final itemLowerCharacter = item.character.toLowerCase();
        final itemLowerMeaning = item.meaning.toLowerCase();
        final queryLower = query.toLowerCase();
        return itemLowerCharacter.contains(queryLower) || itemLowerMeaning.contains(queryLower);
      }).toList();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: MediaQuery.of(context).size.width / 3 * 2,

      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          TextField(
            controller: _controller,
            decoration: InputDecoration(
              labelText: 'Search 汉字 or meaning ',
              border: OutlineInputBorder(borderRadius: BorderRadius.all(Radius.circular(30))),
              contentPadding: EdgeInsets.symmetric(vertical: 10, horizontal: 15),
              suffixIcon: IconButton(
                icon: Icon(Icons.search),
                onPressed: () {
                  _controller.clear();
                  _filterItems('');
                },
              ),
            ),
            onChanged: (value) {
              _filterItems(value);
            },
          ),
          // SizedBox(height: 10),
          if (_isTyping && _filteredItems.isNotEmpty) // Show container only if typing and filtered items are not empty
            Container(
              height: 150, // Fixed height for the SingleChildScrollView
              child: SingleChildScrollView(
                child: Container(
                  padding: EdgeInsets.all(10.0),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    boxShadow: [
                      BoxShadow(
                        color: Colors.grey.withOpacity(0.5),
                        spreadRadius: 2,
                        blurRadius: 5,
                        offset: Offset(0, 3),
                      ),
                    ],
                  ),
                  child: ListView.builder(
                    shrinkWrap: true,
                    itemCount: _filteredItems.length,
                    itemBuilder: (context, index) {
                      return ListTile(
                        title: Text(_filteredItems[index].character),
                        subtitle: Text(_filteredItems[index].meaning),
                        onTap: () {
                          int originalIndex = widget.items.indexOf(_filteredItems[index]);
                          widget.onItemSelected(originalIndex);
                          _controller.clear();
                          _filteredItems.clear();
                        },
                      );
                    },
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}
