import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:iqra_chinese/Audio/textToSpeechController.dart';
import 'package:iqra_chinese/Common_View/MainView.dart';
import 'package:iqra_chinese/Test/WordViewBuilder.dart';
import 'package:iqra_chinese/Archieve/wordList_controller.dart';
import 'package:iqra_chinese/Audio/textToSpeechService.dart';

final EnTextToSpeechService ttsServiceEn = EnTextToSpeechService();
final CnTextToSpeechService ttsServiceCn = CnTextToSpeechService();

void main() {
  runApp(IQraChinese());
}

class IQraChinese extends StatelessWidget {
  IQraChinese({super.key});

  @override
  Widget build(BuildContext context) {
    final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();
    WordListController wordListController = Get.put(WordListController());
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'iQra chinese - learn, review & access your skill',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: MainScreen(),
    );
  }
}

class MainScreen extends StatefulWidget {
  @override
  _MainScreenState createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();
  int _selectedIndex = 0;

  static const List<Widget> _widgetOptions = <Widget>[
    WordViewBuilder(),
    Text('Sentence Page', style: TextStyle(fontSize: 35, fontWeight: FontWeight.bold)),
    Text('Exam Page', style: TextStyle(fontSize: 35, fontWeight: FontWeight.bold)),
  ];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _scaffoldKey,
      appBar: AppBar(
        actions: [
          IconButton(
            icon: Icon(Icons.menu),
            onPressed: () {
              _scaffoldKey.currentState!.openEndDrawer();
            },
          ),
        ],
      ),
      endDrawer: Drawer(
        child: ListView(
          padding: EdgeInsets.zero,
          children: [
            DrawerHeader(
              decoration: BoxDecoration(
                color: Colors.blue,
              ),
              child: Text('Drawer Header'),
            ),
            ListTile(
              title: Text('Item 1'),
              onTap: () {
                // Add onTap functionality for drawer items here
              },
            ),
            ListTile(
              title: Text('Item 2'),
              onTap: () {
                // Add onTap functionality for drawer items here
              },
            ),
          ],
        ),
      ),
      body: Center(
        child: _widgetOptions.elementAt(_selectedIndex),
      ),
      bottomNavigationBar: BottomNavigationBar(
        items: const <BottomNavigationBarItem>[
          BottomNavigationBarItem(
            icon: Icon(Icons.wordpress),
            label: 'Word',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.question_answer_sharp),
            label: 'Sentence',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.question_mark),
            label: 'Test',
          ),
        ],
        currentIndex: _selectedIndex,
        selectedItemColor: Colors.amber[800],
        onTap: _onItemTapped,
      ),
    );
  }
}
