# Cloud Task Scheduling with CloudSim

Java simulations of cloud data centers using CloudSim, including a comparison of five task-scheduling algorithms (FCFS, Shortest Remaining Time, Min-Min, Max-Min, Sufferage).

---

## Overview

In a cloud, many **tasks** must run on a few **virtual machines (VMs)**. A VM is a "computer inside a computer" with its own speed.
The way we assign tasks to VMs is called **scheduling**. Good scheduling finishes work faster and keeps all VMs busy.

**CloudSim** is a Java library that *simulates* a cloud, so you can test ideas without paying for real servers.
In CloudSim, a task is called a **cloudlet**, and VM speed is measured in **MIPS** (million instructions per second).

This repository has three parts from the course:

| Folder | What it does |
|---|---|
| `CloudSimAssignment/` | Basics: create data centers, hosts, VMs, and cloudlets. |
| `Cloud_computing_assignement2/` | Network simulation with a **BRITE** topology (a file that describes network nodes and link delays). |
| `CloudComputingProject/` | Main project: five scheduling algorithms compared. |

## What I did (solo project)

- **Part 1:** Two data centers and two cloudlets on VMs with different MIPS. The cloudlet on the 1000-MIPS VM finished in 40 s; on the 500-MIPS VM it took 80 s.
- **Part 2:** 40 VMs and 80 cloudlets, to show queuing when there are more tasks than VMs.
- **Network assignment:** 3 data centers and 3 brokers mapped onto a 10-node BRITE topology. I explained every timestamp in the output using network delay + execution time (for example, start 23.1 + 40000/250 = finish 183.1).
- **Scheduling project:** Ran five algorithms on the same workload (10 cloudlets, 3 VMs with 300, 600, and 400 MIPS) and compared throughput and turnaround time.

## The five algorithms

| Algorithm | Idea in one line |
|---|---|
| **FCFS** (First Come First Serve) | Give tasks to VMs in order, one by one (round-robin). Ignores task size and VM speed. |
| **Shortest Remaining Time** | Send each task to the VM with the least work already assigned. |
| **Min-Min** | Find the task that can finish *earliest* on its best VM; schedule it first. Repeat. |
| **Max-Min** | Find each task's earliest finish time, then schedule the task with the *largest* of those first. Big tasks go first. |
| **Sufferage** | Schedule first the task that would "suffer" most (biggest time loss) if it did not get its best VM. |

Min-Min, Max-Min, and Sufferage all use an **ETC matrix** (Expected Time to Compute): `ETC[task][vm] = task length ÷ VM MIPS`.

Max-Min in `DatacenterBroker.java` (core loop):

```java
// for each unmapped task, find its minimum completion time and best VM
for (int i = 0; i < cloudletNum; i++) {
    if (mapped[i]) continue;
    for (int j = 0; j < vmNum; j++) {
        double ct = r[j] + etc[i][j];      // VM ready time + run time
        if (ct < minCt[i]) { minCt[i] = ct; minVm[i] = j; }
    }
}
// choose the task whose minimum completion time is the LARGEST
for (int i = 0; i < cloudletNum; i++) {
    if (!mapped[i] && minCt[i] > maxMinCt) { maxMinCt = minCt[i]; chosenCl = i; }
}
```

## Results

**Throughput** = number of cloudlets ÷ total finish time (higher is better).
**Average turnaround time** = sum of finish times ÷ number of cloudlets (lower is better).

| Algorithm | Throughput | Avg. turnaround time |
|---|---|---|
| **Max-Min** | **0.0458** | **163.01** |
| Min-Min | 0.0421 | 163.68 |
| Sufferage | 0.0435 | 186.44 |
| Shortest Remaining Time | 0.0384 | 172.35 |
| FCFS | 0.0272 | 185.28 |

**What the results mean:**

- **Max-Min was best** on both measures. Running big tasks first on fast VMs balanced the load.
- **FCFS was worst** because it ignores task size and VM speed.
- **Sufferage** had good throughput but a high turnaround time. With only 10 tasks and 3 VMs, its extra intelligence did not help much.
- The three "batch" algorithms (Min-Min, Max-Min, Sufferage) look at all tasks and all VMs together, so they make better choices than the one-at-a-time algorithms.

## Tech used

Java · CloudSim 3.0.3 · BRITE network topology · Eclipse

## How to run

1. Download **CloudSim 3.0.3** and find `cloudsim-3.0.3.jar`.
2. Import a folder into Eclipse as an existing project, or compile from the terminal.
3. Add the CloudSim JAR to the build path. (The `.classpath` files point to a local path on my computer; change it to your own.)

Example for the scheduling project:

```bash
cd CloudComputingProject/src
javac -cp /path/to/cloudsim-3.0.3.jar -d ../bin org/cloudbus/cloudsim/*.java
java  -cp ../bin:/path/to/cloudsim-3.0.3.jar org.cloudbus.cloudsim.ExtendedExample
```

Put `../bin` **before** the JAR. The project has its own modified `DatacenterBroker.java` (with the scheduling methods), and it must replace the original class from the JAR.

`ExtendedExample` runs all five algorithms one after another and prints a result table for each.

For the network assignment, the code loads the topology from `examples/org/cloudbus/cloudsim/examples/network/assignment2_topology.brite`, so place the `.brite` file at that path (relative to where you run it).

## What I learned

- How a cloud is modeled: data center → host → VM → cloudlet, with a broker that assigns work.
- How VM speed (MIPS) and queuing change finish times.
- How network delay adds to response time in a distributed system.
- How to compare algorithms fairly: same workload, same VMs, clear metrics.
- That the "smartest" algorithm is not always the best; it depends on the workload size.

**Course:** CS 7125 Cloud Computing · Kennesaw State University · Spring 2026
