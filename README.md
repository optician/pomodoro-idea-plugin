# Pomodoro Technique for Intellij Idea 

## Reason
There are a lot of software for the same purpose 
and some have pretty rich functionality (for example blocking chats and email programs). 
What I try to solve besides the aim of technique:
* Own developer metrics in context of concrete project.
* Simple way to obtain a tool. 
  As for me it's always a trouble to remember a name of that suitable program and look up among the web 
  when I move to a new workspace.
  
## Features
At now plugin is extremely simple (MVP buzzword!). 
* Just start timer and it'll show a modal message box after completion.

## How to get
At now only manual build is available.

## How to build
1. Clone project.
2. Open via Intellij Idea.
3. In project settings remove existing module and create new plugin module.
   Choose Intellij Idea SDK.
4. Idea automatically create run settings for plugin but anyway you need to specify created module.
5. Now you can run Intellij Idea with installed plugin in sandbox. 
   Or you can create plugin zip archive via `Build -> Prepare Plugin Module ...`