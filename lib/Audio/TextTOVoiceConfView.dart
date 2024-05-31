// import 'package:flutter/material.dart';
// import 'package:iqra_chinese/Audio/textToSpeechController.dart';
//
// class TextToVoice extends StatefulWidget {
//   @override
//   _TextToVoiceState createState() => _TextToVoiceState();
// }
//
// class _TextToVoiceState extends State<TextToVoice> {
//   final TextToVoiceController _controller = TextToVoiceController();
//
//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//       appBar: AppBar(
//         title: const Text('Text-to-Speech'),
//       ),
//       body: SingleChildScrollView(
//         child: Padding(
//           padding: const EdgeInsets.all(20.0),
//           child: Center(
//             child: Column(
//               children: <Widget>[
//                 TextField(
//                   // controller: _controller.textEditingController,
//                   maxLines: 5,
//                   decoration: const InputDecoration(
//                     border: OutlineInputBorder(),
//                     hintText: 'Enter some text here...',
//                   ),
//                   onChanged: (String newText) {
//                     setState(() {
//                       _controller.text = newText;
//                     });
//                   },
//                 ),
//                 Row(
//                   children: <Widget>[
//                     const Text('Volume'),
//                     Expanded(
//                       child: Slider(
//                         value: _controller.volume,
//                         min: 0,
//                         max: 1,
//                         label: _controller.volume.round().toString(),
//                         onChanged: (double value) {
//                           setState(() {
//                             _controller.volume = value;
//                           });
//                         },
//                       ),
//                     ),
//                     Text('(${_controller.volume.toStringAsFixed(2)})'),
//                   ],
//                 ),
//                 Row(
//                   children: <Widget>[
//                     const Text('Rate'),
//                     Expanded(
//                       child: Slider(
//                         value: _controller.rate,
//                         min: 0,
//                         max: 2,
//                         label: _controller.rate.round().toString(),
//                         onChanged: (double value) {
//                           setState(() {
//                             _controller.rate = value;
//                           });
//                         },
//                       ),
//                     ),
//                     Text('(${_controller.rate.toStringAsFixed(2)})'),
//                   ],
//                 ),
//                 Row(
//                   children: <Widget>[
//                     const Text('Pitch'),
//                     Expanded(
//                       child: Slider(
//                         value: _controller.pitch,
//                         min: 0,
//                         max: 2,
//                         label: _controller.pitch.round().toString(),
//                         onChanged: (double value) {
//                           setState(() {
//                             _controller.pitch = value;
//                           });
//                         },
//                       ),
//                     ),
//                     Text('(${_controller.pitch.toStringAsFixed(2)})'),
//                   ],
//                 ),
//                 Row(
//                   children: <Widget>[
//                     const Text('Language'),
//                     const SizedBox(
//                       width: 20,
//                     ),
//                     DropdownButton<String>(
//                       value: _controller.language,
//                       icon: const Icon(Icons.arrow_downward),
//                       iconSize: 24,
//                       elevation: 16,
//                       style: const TextStyle(color: Colors.deepPurple),
//                       underline: Container(
//                         height: 2,
//                         color: Colors.deepPurpleAccent,
//                       ),
//                       onChanged: (String? newValue) async {
//                         _controller.setLanguage(newValue!);
//                         setState(() {
//                           _controller.language = newValue;
//                         });
//                       },
//                       items: _controller.languages
//                           .map<DropdownMenuItem<String>>((String value) {
//                         return DropdownMenuItem<String>(
//                           value: value,
//                           child: Text(value),
//                         );
//                       }).toList(),
//                     ),
//                   ],
//                 ),
//                 const SizedBox(
//                   height: 20,
//                 ),
//                 Row(
//                   children: <Widget>[
//                     const Text('Voice'),
//                     const SizedBox(
//                       width: 20,
//                     ),
//                     Text(_controller.voice ?? '-'),
//                   ],
//                 ),
//                 const SizedBox(
//                   height: 20,
//                 ),
//                 Row(
//                   children: <Widget>[
//                     Expanded(
//                       child: Container(
//                         padding: const EdgeInsets.only(right: 10),
//                         child: ElevatedButton(
//                           child: const Text('Stop'),
//                           onPressed: () {
//                             _controller.stopSpeaking();
//                           },
//                         ),
//                       ),
//                     ),
//                     Expanded(
//                       child: Container(
//                         child: ElevatedButton(
//                           child: const Text('Play'),
//                           onPressed: () {
//                             _controller.speak(_controller.text);
//                           },
//                         ),
//                       ),
//                     ),
//                   ],
//                 ),
//               ],
//             ),
//           ),
//         ),
//       ),
//     );
//   }
// }
