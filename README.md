# OS Simulation Project

This repository contains the source code for an operating system simulation project developed for the CSEN 602 Operating Systems course at the German University in Cairo, Faculty of Media Engineering and Technology, Spring 2023.

## Project Overview

The project simulates an operating system, focusing on building a correct architecture. It includes components such as a basic interpreter, memory management, system calls, mutexes, and a Round Robin scheduler.

# System Calls

The operating system simulation project supports the following system calls:

1. **Read File**: Read data from a file on the disk.
2. **Write File**: Write text output to a file on the disk.
3. **Print**: Display data on the screen.
4. **Take Input**: Accept text input from the user.
5. **Read Memory**: Read data from memory.
6. **Write Memory**: Write data to memory.

## Mutexes

The simulation includes the following mutexes to ensure mutual exclusion over critical resources:

1. **File Access Mutex**: Ensures mutual exclusion when accessing files.
2. **User Input Mutex**: Controls access to user input.
3. **Output Mutex**: Manages access to the screen output.

## Scheduler

The project implements the Round Robin scheduling algorithm. Each process is assigned a fixed time slice, executing two instructions per time slice. Processes arrive in the following order: Process 1 at time 0, Process 2 at time 1, and Process 3 at time 4.

## Memory Management

The memory is of a fixed size, consisting of 40 memory words. Each word can store one variable and its corresponding data. The memory is allocated for processes at their arrival time, and each process needs enough space for three variables. If the memory is insufficient, the system unloads an existing process to disk. Memory is managed to ensure processes only access their allocated memory block.

## Output

The simulation outputs the following information for evaluation:

- Queues status after every scheduling event.
- Currently executing process.
- Executing instruction.
- Memory status in human-readable format.
- Process ID whenever a process is swapped in or out of disk.
- Format of the memory stored on Disk.

Please ensure that the output is readable and presentable.
