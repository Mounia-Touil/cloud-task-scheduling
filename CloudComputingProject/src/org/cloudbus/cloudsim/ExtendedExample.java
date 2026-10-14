package org.cloudbus.cloudsim;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class ExtendedExample {

    private static int cloudletNum = 10;
    private static int vmNum = 3;

    public static void main(String[] args) {

        String[] methods = {
            "FCFS", "ShortestRemainingTime", "MinMin", "MaxMin", "Sufferage"
        };
        String[] displayNames = {
            "FCFS", "Shortest Remaining Time", "Min-Min", "Max-Min", "Sufferage"
        };

        for (int m = 0; m < methods.length; m++) {

            Log.printLine("\nStarting simulation for: " + displayNames[m]);

            try {
                int num_user = 1;
                Calendar calendar = Calendar.getInstance();
                boolean trace_flag = false;
                CloudSim.init(num_user, calendar, trace_flag);

                Datacenter datacenter0 = createDatacenter("Datacenter_0");

                DatacenterBroker broker = createBroker();
                int brokerId = broker.getId();

                // Create 3 VMs with different MIPS
                List<Vm> vmList = new ArrayList<Vm>();
                int[] mips = {300, 600, 400};
                long size = 10000;
                int ram = 2048;
                long bw = 1000;
                int pesNumber = 1;
                String vmm = "Xen";
                int vmid = 0;

                for (int i = 0; i < vmNum; i++) {
                    vmList.add(new Vm(vmid, brokerId, mips[i],
                        pesNumber, ram, bw, size, vmm,
                        new CloudletSchedulerTimeShared()));
                    vmid++;
                }
                broker.submitVmList(vmList);

                // Create 10 Cloudlets
                List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
                long[] lengths = {
                    18000, 42000, 21000, 32000, 20000,
                    15000, 10000, 30000, 40000, 50000
                };
                long fileSize   = 300;
                long outputSize = 300;
                UtilizationModel um = new UtilizationModelFull();
                int id = 0;

                for (int i = 0; i < cloudletNum; i++) {
                    Cloudlet cloudlet = new Cloudlet(
                        id, lengths[i], pesNumber,
                        fileSize, outputSize, um, um, um);
                    cloudlet.setUserId(brokerId);
                    cloudletList.add(cloudlet);
                    id++;
                }
                broker.submitCloudletList(cloudletList);

                // Apply scheduling algorithm
                switch (methods[m]) {
                    case "FCFS":
                        broker.bindCloudletsToVmsSimple();
                        break;
                    case "ShortestRemainingTime":
                        broker.bindCloudletToVmsShortestRemainingTime();
                        break;
                    case "MinMin":
                        broker.bindCloudletToVmsMinMin();
                        break;
                    case "MaxMin":
                        broker.bindCloudletToVmsMaxMin();
                        break;
                    case "Sufferage":
                        broker.bindCloudletToVmsSufferage();
                        break;
                }

                // Run simulation
                CloudSim.startSimulation();
                List<Cloudlet> newList = broker.getCloudletReceivedList();
                CloudSim.stopSimulation();

                // Print output table
                printCloudletList(newList, displayNames[m]);

                Log.printLine("ExtendedExample finished!");

            } catch (Exception e) {
                e.printStackTrace();
                Log.printLine("Error in simulation: " + methods[m]);
            }
        }
    }

    private static Datacenter createDatacenter(String name) {

        List<Host> hostList = new ArrayList<Host>();
        int hostId  = 0;
        int ram     = 2048;
        long storage = 1000000;
        int bw      = 10000;

        for (int i = 0; i < vmNum; i++) {
            List<Pe> peList = new ArrayList<Pe>();
            peList.add(new Pe(0, new PeProvisionerSimple(1000)));
            hostList.add(new Host(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)));
            hostId++;
        }

        String arch           = "x86";
        String os             = "Linux";
        String vmm            = "Xen";
        double time_zone      = 10.0;
        double cost           = 3.0;
        double costPerMem     = 0.05;
        double costPerStorage = 0.001;
        double costPerBw      = 0.0;
        LinkedList<Storage> storageList = new LinkedList<Storage>();

        DatacenterCharacteristics characteristics =
            new DatacenterCharacteristics(
                arch, os, vmm, hostList,
                time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(
                name, characteristics,
                new VmAllocationPolicySimple(hostList),
                storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datacenter;
    }

    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    private static void printCloudletList(
            List<Cloudlet> list, String algoName) {

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT: " + algoName + " ==========");
        Log.printLine(
            "Cloudlet ID" + indent +
            "STATUS"      + indent +
            "DC ID"       + indent +
            "VM ID"       + indent +
            "Time"        + indent +
            "Start Time"  + indent +
            "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");

        for (Cloudlet cloudlet : list) {
            Log.print(indent + cloudlet.getCloudletId()
                + indent + indent);
            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");
                Log.printLine(
                    indent + indent
                    + cloudlet.getResourceId()
                    + indent + indent + indent
                    + cloudlet.getVmId()
                    + indent + indent
                    + dft.format(cloudlet.getActualCPUTime())
                    + indent + indent
                    + dft.format(cloudlet.getExecStartTime())
                    + indent + indent
                    + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
}