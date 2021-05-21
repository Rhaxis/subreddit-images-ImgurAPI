# subreddit-images-ImgurAPI

|                  |                                                                                                                                                                                                                                                                            |
| ---------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Name             | Ville Ekholm                                                                                                                                                                                                                                                              |
| Topic            | Mobile application that shows 50 latest Imgur pictures from a given subreddit. User can also choose a random subreddit and sort pictures. Done by using Imgur API |
| Target           | Android/Kotlin                                                                                                                                                                                                                                                              |

**Release 1: 2021-05-12 features**


  - User can type any subreddit name (for example "cats", "gifs", "animals") and the app will fetch 50 newest imgur images, gifs and videos from that subreddit.
  - User can press random button to fetch 50 newest imgur images, gifs and videos from a random subreddit. Currently there is only ~10 different subreddits available.
  - App will downscale images according the their size to decrease memory usage ( big images will be downscaled more and small images wont have any scaling).
  - Search and random buttons are disabled until the UI is done updating.
  - If the given subreddit is not found or it simply has no imgur content the app will give an error.

    **Known bugs**: None at the moment.
      
        
          
            
              
                
**Release 2: 2021-05-21 features**
  
  
   **Screencast:** https://www.youtube.com/watch?v=6D_UjYa9N_U
  
  - Added a dropdown menu where user can choose how many items are shown at a time.
  - Refactored code
  - Added comments
  - Improved visual style of the app

  **Known bugs**: App has no known bugs
