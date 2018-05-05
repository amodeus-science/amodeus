#!/bin/bash

# Download source code
mkdir -p /home/$USER/glpk_src
cd /home/$USER/glpk_src
rm -rf glpk-4.65*
wget --quiet http://ftp.gnu.org/gnu/glpk/glpk-4.65.tar.gz
tar -xzf glpk-4.65.tar.gz
rm -rf glpk-java-1.12.0*
# wget http://download.sourceforge.net/project/glpk-java/\
# glpk-java/glpk-java-1.12.0/libglpk-java-1.12.0.tar.gz

wget --quiet https://polybox.ethz.ch/index.php/s/JjWmFgYNQT4O6Bx/download
mv download libglpk-java-1.12.0.tar.gz

tar -xzf libglpk-java-1.12.0.tar.gz

# Build and install GLPK
cd /home/$USER/glpk_src/glpk-4.65
./configure --prefix=/home/$USER/glpk > /dev/null
make --quiet -j6
make --quiet check
make --quiet install

# Build and install GLPK for Java
cd /home/$USER/glpk_src/libglpk-java-1.12.0
export CPPFLAGS=-I/home/$USER/glpk/include
export SWIGFLAGS=-I/home/$USER/glpk/include
export LD_LIBRARY_PATH=/home/$USER/glpk/lib
./configure --prefix=/home/$USER/glpk > /dev/null

# read -n 1 -s -r -p "Press any key to continue"

make --quiet
make --quiet check
make --quiet install
unset CPPFLAGS
unset SWIGFLAGS

# Build and run example
#cd /home/$USER/src/libglpk-java-1.12.0/examples/java
#$JAVA_HOME/bin/javac \
#-classpath /home/$USER/glpk/share/java/glpk-java-1.12.0.jar \
#GmplSwing.java
#$JAVA_HOME/bin/java \
#-Djava.library.path=/home/$USER/glpk/lib/jni \
#-classpath /home/$USER/glpk/share/java/glpk-java-1.12.0.jar:. \
#GmplSwing marbles.mod

