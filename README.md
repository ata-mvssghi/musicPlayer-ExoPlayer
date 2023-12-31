# musicPlayer-ExoPlayer

## Overview

Welcome to the Music Player App repository! This Android application is designed to provide users with a seamless music playback experience. The app allows users to grant permissions to access their internal storage, fetch songs, and organize them in a user-friendly interface.

## Features

- **Song List Fragment:** Displays a list of songs fetched from the user's internal storage. The list is presented in a Recycler View within a Songs List Fragment.
  
- **Song Preview Fragment:** Features a Seek Bar and control buttons (play, pause, next, back) to facilitate playback control. This fragment is designed to provide a preview of the currently playing song.

- **Service Class:** Utilizes a service class to enable push notifications and ensure that the app continues playing songs even after it has been closed. This enhances the user experience by allowing seamless music playback.

- **Notification with Custom Layout:** When a user clicks on a song in the Song List Fragment, a custom notification is built. This notification includes play, pause, next, and back buttons, along with an image of the current song being played.
  
- **Add songs to favorite -> storing the favortie songs in in room data base and displaying them in another fragment called "FavoriteFragment"
  
- **Searching in songs via room library and quering for songs assigned as favorite songs

- **Exo Player Library:** The app integrates the "Exo Player" library as the primary player for handling audio playback. This powerful library enhances the app's capabilities and ensures a robust music streaming experience.

- **Card View Layout:** Each item in the Recycler View is wrapped in a Card View. This Card View contains essential details about the song, including the song name, singer, album, and an image of the song.

## Setup

To run the Music Player App locally, follow these steps:

1. Clone the repository to your local machine:
    ```bash
   https://github.com/ata-mvssghi/musicPlayer-ExoPlayer.git

2. Open the project using Android Studio.

3. Build and run the application on an Android emulator or device.



![Screenshot_2023-10-15-15-42-05-172_com example muiscplayerproject](https://github.com/ata-mvssghi/musicPlayer-ExoPlayer/assets/99190904/89efae55-e1bd-4862-94f2-27201c465eae)

![Screenshot_2023-10-15-15-42-11-113_com example muiscplayerproject](https://github.com/ata-mvssghi/musicPlayer-ExoPlayer/assets/99190904/fdedcde2-1604-4914-910f-f643e27e3168)

![Screenshot_2023-10-15-15-41-27-453_com example muiscplayerproject](https://github.com/ata-mvssghi/musicPlayer-ExoPlayer/assets/99190904/34aa6866-cba8-4052-b9c9-2c23e43781a5)

![Screenshot_2023-10-15-15-42-28-344_com example muiscplayerproject](https://github.com/ata-mvssghi/musicPlayer-ExoPlayer/assets/99190904/c86c83df-3f3b-487c-91a4-bd1c48962c5c)

## Contributing

We welcome contributions to enhance the functionality and features of the Music Player App. Feel free to submit issues or pull requests.


## Contact

For any questions or concerns, please contact the project maintainer:

- Ata-Mvssghi
- email: atamovassagi@gmail.com
-  GitHub Profile: https://github.com/ata-mvssghi

Happy coding! 🎶
