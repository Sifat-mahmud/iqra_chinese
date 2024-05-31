// import 'package:flutter/cupertino.dart';
// import 'package:flutter/material.dart';
//
// import '../Test/WordViewBuilder.dart';
//
// class MainVIew extends StatefulWidget {
//   const MainVIew({super.key});
//
//   @override
//   State<MainVIew> createState() => _MainVIewState();
// }
//
// class _MainVIewState extends State<MainVIew> {
//   @override
//   Widget build(BuildContext context) {
//     return Scaffold(
//       key: _scaffoldKey,
//       appBar: AppBar(
//         actions: [
//           IconButton(
//             icon: Icon(Icons.menu),
//             onPressed: () {
//               // Open the right-side drawer when the menu icon is clicked
//               _scaffoldKey.currentState!.openEndDrawer();
//             },
//           ),
//         ],
//       ),
//       // Use endDrawer for the drawer to appear from the right side
//       endDrawer: Drawer(
//         // Add your drawer content here
//         child: ListView(
//           padding: EdgeInsets.zero,
//           children: [
//             DrawerHeader(
//               decoration: BoxDecoration(
//                 color: Colors.blue,
//               ),
//               child: Text('Drawer Header'),
//             ),
//             ListTile(
//               title: Text('Item 1'),
//               onTap: () {
//                 // Add onTap functionality for drawer items here
//               },
//             ),
//             ListTile(
//               title: Text('Item 2'),
//               onTap: () {
//                 // Add onTap functionality for drawer items here
//               },
//             ),
//           ],
//         ),
//       ),
//       body: Center(
//         // Add your body content here
//         child: Text('Your Body Content'),
//       ),
//     );
//
//   }
// }
