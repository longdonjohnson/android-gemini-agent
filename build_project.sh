#!/bin/bash
# Script to create Android Gemini Agent project structure

mkdir -p app/src/main/{java/com/gemini/agent,res/{layout,values,xml},assets}
mkdir -p app/src/main/java/com/gemini/agent/{service,client,ui,models,utils}

echo "Android Gemini Agent project structure created!"
echo "Directory: ~/android-gemini-agent"
