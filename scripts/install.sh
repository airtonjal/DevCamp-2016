#!/bin/sh

install_brew() {
  if ! type "brew" > /dev/null; then
    echo "brew not found, installing"
    ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
  else
    echo "brew installed, skipping it"
  fi
}

brew install zookeeper kafka spark 
