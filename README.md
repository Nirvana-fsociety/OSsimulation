# 如果README.md中的图片无法查看

如果无法查看README.md中的图片，请在本地文件位置：C:\Windows\System32\drivers\etc\hosts把hosts文件用管理员权限打开，

并在其末尾加上一行

199.232.68.133		raw.githubusercontent.com

并保存即可打开。

# 可视化仿真实现作业管理与虚页内存管理

## 摘要

本人使用Java程序设计语言仿真实现可视化仿真实现作业管理与虚页内存管理。

在项目中，本人力求设计思想贴近教材，从而达到深入理解教材中的基础知识。在实践过程中理解诸如死锁检测算法、死锁恢复方式、伙伴算法分配连续内存页框、LRU页面替换算法等操作系统知识。

## 主要完成的功能

能够支持8个进程运行的操作系统仿真程序。

界面的可视化。可视化部分包括：三级调度的可视化、伙伴算法动态回收和分配内存页框的仿真、指令执行过程和系统运行过程的运行细节报告、CPU、MMU、DMA部件、页表。

整个系统由6个线程的共同构成。运行过程包括：进程指令执行与三级调度线程、时钟及死锁检测线程、DMA传输线程、作业管理线程、请求创建作业线程、界面刷新线程。

模拟了计算机系统硬件，包括：地址总线、数据总线、CPU、MMU、时钟、DMA、主存、辅存。在计算机硬件模拟的基础上实现了三级调度。 

在三级调度的过程中，实现了进程原语，包括：挂起、激活、阻塞、唤醒、撤销、创建。也实现了MMU地址转换、页表的生成。

拥有5种详细设计的系统特权程序：Ｐ操作、V操作、缺页异常处理、DMA赋值启动程序、DMA输入善后程序。以及４种转为系统调用触发指令设计的无实际内容的普通系统调用。还实现了三类中断处理程序：包括DMA传输完成中断处理、时钟中断处理以及死锁检测中断处理程序。

在存储管理过程中实现了通过伙伴算法分配回收内存物理页框以及基于LRU算法的虚页存储管理。在进程调度过程中实现了死锁检测与死锁恢复。同时还实现了进程之间的互斥争夺资源，以及外部设备与进程之间的同步。其中进程调度全程由执行的指令集来决定，本人设计的指令较为细致具体，包括访存指令、输入指令、计算指令、输出指令、跳转指令、系统调用触发指令以及一条普通指令。每一种指令都能触发不同的系统行为。

## 核心设计

本人作品的核心设计为三个部分：进程管理系统、详细的指令系统和独特的资源管理器。

进程管理子系统是系统的核心部分，根据当前执行的指令，通过进程原语，对进程进行三态调度，在逐条执行指令的过程中触发系统调用及包括死锁检测在内的中断处理程序，根据内存资源的充裕程度还能触发中级调度。进程管理子系统包含了存储管理子系统，存储管理子系统的核心是伙伴算法分配物理页框和基于LRU算法和缺页异常处理的虚存管理。

独特的指令集系统在不超出设计范围的前提下尽量丰富、详细且具体，每种指令都能促成系统的状态转换和多种系统特权程序执行等不同处理行为。

资源管理器则对进程之间争夺的资源进行集中的管理，并且负责死锁检测程序和PV操作的具体实现。

## 关键词
进程管理、死锁检测、指令系统、可视化、虚页管理。

## 1	实践内容与目标

### 1.1	实践内容

本此课程设计的时间内容为“可视化仿真实现作业管理与虚页内存管理”，内容主要为通过多个作业创建为对应进程在系统中调度，从而仿真实现操作系统中的多任务并发环境。

1)	仿真实现作业及进程的并发环境，本人实现了最多八个进程同时进入系统参与调度；

2)	MMU地址变换，本人实现了通过CPU发送逻辑地址给MMU并在MMU过程中转换为物理地址、然后根据得到的物理地址访存的过程，其中还实现了缺页异常及其处理程序。

3)	实现的进程原语主要包括：挂起、激活、阻塞、唤醒、撤销、创建。而进程的三级调度就是依赖于上述进程原语实现的。

4)	实现了虚存管理，实现的基础是缺页异常及其处理程序及LRU页面替换算法。

5)	实现了物理内存的分配与回收，主要依赖于伙伴算法实现。

6)	本人实现了5种详细设计的系统特权程序：PV操作、缺页异常处理、DMA赋值启动程序、DMA输入善后程序。以及一种转为系统调用触发指令设计的无实际内容的普通系统调用。其中以PV操作为基础实现了进程间的互斥争夺资源。

7)	此外，本人还实现了3种精心设计的中断处理程序，包括时钟中断处理程序、DMA中断信号处理程序和死锁检测处理程序。其中死锁检测处理程序用在了实现死锁检测的过程中。

8)	同时本人还依照计算机底层指令集的格式设计了一个拥有７种指令的指令系统，包括输入指令、输出指令、访存指令、计算指令、系统调用触发指令和普通指令。其中输入输出指令都依赖于DMA于请求输入进程的生产者消费者同步实现，而前５种指令都会涉及到PV操作从而互斥争夺资源。访存指令还能触发存取数据时的缺页异常。系统调用触发指令则特意为了触发一般的系统调用而设计。普通指令则模拟量占用１秒运行实际的空操作。

9)	参照教材上对管程的描述，实现了一个类似于管程的资源管理器。用于集中管理进程争夺的有限资源。此外该资源管理器也是死锁检测和PV操作的实际操作模块。

10)	实现死锁检测和死锁恢复：分别依赖于根据时钟信号定时发起的死锁检测程序和发现死锁后的恢复操作。本人在设计死锁恢复的过程中，摒弃了代价较大、但是简便易行的撤销所有死锁进程的做法，而是采用了代价较小、但是实现起来较为复杂的剥夺死锁进程资源并回滚一条指令的策略。

11)	此外，本人还顺利实现了可视化过程，包括进程控制块、PCB池、就绪队列、阻塞队列、挂起就绪队列、挂起阻塞队列以及当前运行PCB的可视化。另外还实现了主要硬件：时钟、CPU内部关键寄存器、MMU内部结构（包括快表TLB）、主存、DMA内部结构的可视化。其中对主存的可视化主要是通过讲伙伴算法的运行过程动态显示实现的。另外还通过细致复杂的页表结构实现了虚存管理的可视化。还实现了系统运行过程的细节报告可视化，即显示出日志文件的生成过程。

### 1.2	实践目标

本人的实践目标是，通过实际的Java程序，尽可能依照教材实现一个完整而全面的仿真操作系统软件。在实现过程中尽量符合操作系统的理论，做细致详细合理的设计。在实践过程中将操作系统中抽象的算法，如：伙伴算法分配回收内存页框、死锁检测算法如何发现死锁、LRU算法如何在实际中替换驻留集中的页面，通过代码实现显得具象，并使得本人能够深入理解其中的原理。

该系统的功能模块划分为：

1)	计算机底层硬件模块：为多进程管理的系统环境提供硬件支持；

2)	作业管理子系统：将未来请求序列中的请求创建为作业，并维护作业后备队列。

3)	存储管理子系统：为多进程管理的实现提供内存分配与回收、虚页管理、数据存储指令读取等功能支持。

4)	资源管理器：一个类似于管程的资源管理器，对各种资源进行集中管理，每一个资源对应一个或多个信号量，每个资源设定了特定的数量，并建立了等待该资源的进程等待队列。并且负责实现PV操作，死锁检测的实现。

5)	指令系统：详尽具体的指令系统应用于系统运行过程中，能够完全发挥操作系统的功能，一条指令复杂到能促使进程实现多次三态转换，细致到执行一条指令从开始到结束可以触发多种系统调用指令。

6)	进程管理系统：基于上述硬件模块、存储管理系统、资源管理器和指令系统这四大功能模块，对进程进行三级调度，从而实现多任务并发的系统环境。


## 2 硬件仿真设计

### 2.1	CPU

本人设计的CPU包括程序计数器PC、指令寄存器IR、状态位寄存器PSW、地址寄存器AR和栈顶指针寄存器SP。每个寄存器存储的数据都为16bit，但是功能各不相同

#### 2.1.1	程序计数器PC

主要功能是存放要执行的下一条指令的逻辑地址，并且有自动加一的功能和接收外来数据和返回PC内部数据的功能。

#### 2.1.2	指令寄存器IR

主要功能是存放当前执行的指令内容，并且拥有从16位二进制指令中提取指令表达的信息的功能。对于任何指令，IR都提取其高三位识别码先识别该指令种类，在得到指令种类之后再根据其种类提取识别码后面的操作字段。对于不同的指令IR寄存器提取的操作字段不同：

1)	如果是输入指令或输出指令，IR能够提取外存文件地址；

2)	如果是系统调用指令，IR能够提取系统调用编号；

3)	如果是访存指令，能够提取目的寄存器编号和以进程映像起始逻辑地址为基址的访存偏移量；

4)	如果是计算指令，则能够提取目的寄存器编号和源寄存器编号；

5)	如果是跳转指令，则能够提取以当前PC内存储指令地址为基址的偏移量；

6)	如果是普通指令，则没有访问字段。

经过对指令的字段提取，就可以按照获得的指令信息执行指令。

#### 2.1.3	状态位寄存器PSW

再仿真系统实现的过程中，只负责记录当前CPU的状态是内核态还是用户态。提供系统用户切换的功能。

#### 2.1.4	地址寄存器AR

因为本人设计来访存指令，而访存指令就要读取内存中数据段的某一个位置，如果数据段尚未被调入，还可以触发缺页异常，并且切换为内核态执行缺页异常处理程序，所以必须明确每一条访存指令访存的逻辑地址。AR寄存器就是为了存放用于访存数据段的16位逻辑地址而设计的。提供将地址写入AR寄存器和从AR寄存器读出地址的功能。

#### 2.1.5	栈顶指针寄存器SP

SP寄存器同样存储16位逻辑地址，指示当前运行的进程保存现场的核心栈的栈顶所在的逻辑地址单元。提供有压栈过程中的地址自增、弹栈过程中的地址自减和读取写入地址的功能。

### 2.2	MMU

MMU的主要结构为用于输入的逻辑地址寄存器和用于输出物理地址寄存器，存放当前运行进程的页表基址的寄存器。以及快表TLB。MMU的工作方式为从MMU外部（CPU的PC寄存器或AR寄存器）输入要访问的16位逻辑地址，然后再MMU内部对该地址解析出7位逻辑页号和9位地址偏移。根据逻辑页号查找快表，得到该逻辑页号当前对应的6位物理页号，将得到的物理页号与9位地址偏移拼接成15位物理地址存入物理地址寄存器中，向MMU外部的地址总线输出。

当然，在查找快表的过程中并不总是能够成功找到逻辑页号对应的物理页号。如果查找失败，将求助于MMU外部的页表，在页表中寻找物理页号。如果仍旧失败，就会触发缺页异常。上述过程的参考依据为教材220页对MMU结构的介绍。

### 2.3	地址总线

地址总线用于在硬件结构之间传送地址，包括逻辑地址的传送和物理地址的传送。CPU内部AR寄存器和PC寄存器中的逻辑地址将通过地址总线送往MMU的逻辑地址寄存器。MMU成功转换的物理地址将通过地址总线传送给内存，用于寻访内存的双字节存储单元。

### 2.4	数据总线

数据总线用于传输来自内存的进程正文段中的16位指令内容以及访存指令存取的16位数据。如果是指令内容，则送往CPU的指令寄存器IR，以供取指令后执行指令使用。如果是访存指令访存的数据，由于并没有设计通用寄存器没有位置可以存放，且访存指令的意义在于触发PV操作和数据访存过程，去除的数据没有实际价值，所以选择直接丢弃。 

### 2.5	时钟

本人模拟的时钟具有多种功能：模拟了用来提示时间片已耗尽的时钟中断信号，检测后备作业队列是否有作业排队的提醒信号，死锁检测算法的检测时间提醒信号。目前本人设计的时间片长度位４秒，检测作业后备队里的时间长度位５秒，检测是否发生死锁的时间长度为９秒。在这些时刻，时钟都会向CPU发出硬件中断信号，并要求CPU停下当前的工作进行处理。

### 2.6	DMA

DMA是一个独立运行的处理器，能够与CPU同时运行。它主要负责CPU执行输入输出指令时的独立传输工作，进程只需要为DMA提供要读取的外设所在外存地址，传输方向、要传输字数和系统缓冲区地址然后触发DMA启动即可，CPU转而运行其他无关进程。DMA的具体的数据传输操作在CPU运行其他进程的同时进行。

基于上述功能设计，以及教材２５７页IO控制方式中DMA方式的介绍，本人模拟DMA的主要部件包括：系统缓冲区的物理地址寄存器、存放进程用户缓冲区物理地址的主存地址缓冲区。存放外设文件在外存中的地址的外设地址寄存器。计数从外设文件搬移到缓冲区的双字节数据个数的数据计数器。能在传输工作完成后向CPU汇报工作完成的中断信号发送部件。用于接收CPU启动信号的CPU信号接收部件。方向信号部件，CPU通过设置该部件的值来控制数据的传输方向，如果值是“真”表示CPU让DMA发起将系统缓冲区中数据搬运到外设所在文件。如果是“假”表示CPU让DMA将外设文件中的数据读入系统缓冲区。

### 2.7	主存

主存设计中，物理块总数为６４块，每块物理页框大小为５１２B。

#### 2.7.1	系统区

主存中系统区内存占用固定的３２块：０号页框～３１号页框。进一步细致划分为：


![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/chart1.png)

其中PCB池中一个PCB的尺寸大小为６４个双字节存储单元，所以容纳八个PCB的PCB池需要２个物理页框。模拟一个特权指令集的长度为１２８个双字节存储单元，其中包括３种硬件中断处理程序、５种本人详细设计的异常处理程序和４种没有实际操作的普通系统特权程序。共１２个，所以占用６页。

#### 2.7.2	用户区
	
用户区同样占用固定的３２块：３２号～６３号页框。其中每个进程允许主流在内存中的驻留集大小为一个进程８块物理内存。每个进程映像包括一个PCB进程控制块（存放在系统区），一个４页大小的进程正文段，一个２页大小的数据段，一个１页大小的用于保存现场的核心栈，一个１页大小的进程用户缓冲区，共８页的进程映像。在实际分配的过程中根据预调页操作和缺页异常处理两个方式动态分配用户区的内存块。

### 2.8	辅存

按照要求设置为２０４８个辅存扇区，我们将其分为３２个磁道，每个磁道分得６４个扇区，每个扇区按照设计要求，与主存物理页框等大，即５１２B。

对这２０４８个扇区的细致划分如下：

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/chart2.png)

其中，设备文件区和作业存储文件区被包括在文件区中。交换区与文件区共同构成整个辅存。

以上就是本人的全部硬件仿真内容，强调只设计有关于操作系统运行调度的部分，不刻意追求实现到最真实，但是完全能够作为支持操作系统运行的基础而不出现任何问题。

## 3	数据结构与基础操作的抽象与设计
### 3.1	用户指令集系统

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/chart3.png)

#### 计算指令执行详细机制

计算指令的操作字段分别是目的寄存器编号和源寄存器编号。功能是将两个寄存器中的数据进行运算。本人的设计原则是只体现操作系统的运行，至于真正的计算操作比如两个具体的数相加并不考虑实现。所以将通用寄存器抽象为表示互斥争夺资源的信号量，不做计算。

首先阻塞自己，CPU转为内核态，触发系统P操作，让系统去替该进程申请源寄存器。如果申请获得允许，就唤醒该阻塞进程，如果不被允许，则一直阻塞，知道申请成功唤醒。

唤醒后的进程再次回到CPU运行。有一次阻塞自己，CPU转为内核态，触发P操作，让系统替它申请目的寄存器，同样如果申请运行就唤醒，如果不允许就一直阻塞。

当进程再次回到CPU继续执行计算时已经占用了两个寄存器，这是通过延时操作占用５００毫秒的计算时间，然后阻塞自己，CPU转为内核态，触发V操作，让系统替它释放目的寄存器。如果释放成功就唤醒，否则一直阻塞。

再次回到CPU，进程阻塞，转为内核态，系统释放源寄存器，成功后唤醒该进程。该进程再回到CPU时计算指令执行完毕，取下一条指令执行。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/操作系统报告流程图-计算.png)

#### 访存指令执行详细机制

访存指令有两个操作字段分别是以进程映像起始逻辑地址为基址的访存偏移和目的寄存器编号。功能是：从指令指定的进程数据段位置读出数据写入指令指定编号的通用寄存器。

首先，第一次执行就触发了系统调用，在系统调用前阻塞当前进程，系统转为内核态，由系统去替该进程执行P操作，通过P操作来尝试申请“指定编号的目的寄存器”资源，系统根据IR寄存器中原先需要的寄存器号去申请相应的寄存器。这时申请的进程已经在阻塞队列中，它等待系统P操作后的结果，如果系统为其申请到了该项资源的使用许可，就将这个进程唤醒，送入就绪队列，当它进入CPU运行时，就可以使用这个资源了。如果没有得到许可，该进程就一直阻塞，知道申请成功。

再次进入CPU后，进程会根据提取得到的偏移量与当前进程的逻辑地址起始位置相加得到该进程数据段中的某一访存位置。然后送MMU进行访存。其中如果该逻辑地址对应的数据没有调入物理内存，那么就会触发缺页异常阻塞该进程，并由系统执行缺页异常处理程序（细节将在后文介绍）。

如果访存成功，访存指令的任务就完成了，下面访存指令将释放该进程占用的目的寄存器资源。该释放请求将触发V操作，同时阻塞自己。当系统帮助他释放占用的寄存器，如果释放成功则唤醒该进程。之后进程会再次进入CPU运行，此时访存指令执行完成，并取下一条指令执行。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/操作系统报告流程图访存.png)

#### 输入指令执行详细机制

操作字段是设备文件的外存地址。功能为将外设文件所在的一个扇区的数据经过系统缓冲区最终搬运到用户进程缓冲区。

首先进程阻塞自己，CPU转为内核态，并触发系统P操作申请外部设备，如果申请被允许就唤醒该进程。唤醒后再运行的，阻塞自己并再次触发P操作申请系统缓冲区的控制权。再次唤醒进入CPU运行，就阻塞自己并转为内核态，让系统替进程执行DMA的赋值程序，并启动DMA让其传输数据。上述DMA赋值也是一个系统调用（后文详细介绍）。

再DMA赋值程序结束后，系统并不将该进程唤醒，而是让该阻塞进程继续在阻塞队列中等待，等待的是DMA将外设文件数据搬运到系统缓冲区。而在DMA搬运数据工作的同时，系统选择其他无关进程进入CPU运行。直到DMA搬运工作完成，会像CPU发出硬件中断信号，CPU接到信号后，立即冻结当前CPU中运行的无关进程，并转为内核态执行DMA中断处理程序——唤醒之前因为等待DMA传输而阻塞的进程。然后回到用户态继续执行无关进程。

当被唤醒的进程回到CPU执行时，阻塞自己并转为内核态，触发输入善后程序（另一个系统调用，后文详细介绍），系统在该程序中将系统缓冲区中的数据搬运到进程的用户缓冲区中。然后唤醒进程。

进程再次回到CPU时不仅占有系统缓冲区、指定占用的外设、还拥有来自外设文件中的数据，此时输入指令是指上已经完成。接下来就阻塞自己，触发系统V操作释放系统缓冲。再次进入CPU则触发系统V操作释放外设。指令到此就运行结束。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/操作系统报告流程图-输入.png)

#### 输出指令执行详细机制
	
对于输出指令，与输入指令的机制几乎相同，唯一有两点不同之处：DMA搬运的方向是将系统缓冲区中的数据搬运到外设文件中，是清空缓冲区不是填满缓冲区；在DMA完成清空缓冲区数据后，进程不做善后处理，通过触发系统V操作，释放自己占用的系统缓冲区和外设即可。

#### 跳转指令执行详细机制
	
跳转指令的操作字段是以PC寄存器中存储的地址为基址的取指令偏移。功能为：当执行到该跳转指令时，将PC自增一个跳转指令提取到的偏移量即可。下一次取指令将以跳转后的第一条指令开始。

#### 指令系统总结

上述文字介绍了本人设计的6中指令的格式，并且就其中着重表述了计算指令、访存指令、输入指令三种最为复杂的指令，而跳转指令较为简单，输出指令与输入指令原理相同，所以简要介绍。

### 3.2	作业控制块JCB

作业控制块包括作业号，作业优先级，指令集在外存中存放的地址，作业拥有的指令数，数据集在外存中存放的地址，数据集中数据量。主要用于管理根据请求创建的作业的指令集和数据集，主要存放在作业后备队列中。根据本人的设计，时钟每走５秒的时间，就会检测一下后备作业队列中是否有等待创建为进程的JCB作业控制块，如果有则检测是否能够创建为进程，如果可以则创建。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/JCB.png)

### 3.3	进程控制块PCB
	
进程控制块主要包括进程标识、现场信息、控制信息三大部分。位于系统区内存的PCB池中。在系统进行进程的三级调度时，主要操作的就是进程的PCB部分。通过PCB部分唯一确定一个进程，并且从中得到所有进程的必要信息。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/PCB.png)

其中进程标识用来与其他同在系统中的进程做区分。

另外现场信息保存的是进程从运行态转变为其他状态（就绪态和阻塞态）时需要保存离开前CPU的现场信息，即CPU中关键寄存器的内容，以便再次进入CPU运行时能够继续运行。这里用PCB保存现场信息与进程核心栈保存现场信息是有区别的（见后文进程映像部分）。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/PCB-1.png)

结构最为复杂的是PCB的控制信息部分，也是存储关键信息最多的部分。包括：

#### 进程调度信息

1)	进程状态（阻塞、就绪、运行、挂起就绪和挂起阻塞5种状态）；

2)	阻塞原因（DMA输入等待、DMA输出等待、等待源寄存器、等待目的寄存器、等待释放源寄存器、等待释放目的寄存器、等待系统缓冲区、等待释放系统缓冲区、数据段访存缺页异常、正文段访存缺页异常、等待外设、等待释放外设、触发一般系统特权程序、等待DMA输入善后共14种阻塞原因）；

3)	进程优先级（1~10范围内的静态优先级）。

#### 进程组成信息

1)	进程专属页表在系统区内存的物理起始地址；

2)	正文段指针（进程映像的正文段逻辑首地址）；

3)	数据段指针（进程映像的数据段逻辑首地址）；

4)	核心栈栈底（进程核心栈的栈底逻辑地址）；

5)	进程缓冲区地址（主要用于输入输出指令种的数据传输）；

6)	进程拥有指令数；

7)	进程拥有数据量；

8)	进程拥有的页表项数；

9)	进程专属交换区的首地址（用于要挂起进程把自己的全部映像都挂到外存的该位置，所以长度为进程整个映像的长度——一个进程8页）。

#### CPU使用统计信息

1)	进程访问字段（每次进入CPU运行时该字段清零，同时其他进程的该访问字段加一，用于中级调度时，统计哪一个进程最近最少被访问，选择访问字段值最高的进程挂起，相当于一个以进程为单位的类似LRU算法，因为真正的LRU算法是用在虚页管理的页面替换过程中。）；

2)	已占用CPU时间（自从本此进入CPU以来，在CPU中运行的时间）；

3)	进程已运行时间的总和（自从进程被创建以来，在CPU中运行的时间总和）。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/PCB-2.png)

### 3.4	进程映像
进程映像共由5部分构成：PCB、正文段、数据段、用户缓冲区和核心栈。PCB已在上文提到，下面介绍其他4个部分。
#### 正文段

正文段中主要存放进程要运行的指令集。在进程创建时不调入，而是在进程运行时通过取指令，发现该指令所在的页并未从外存调入物理内存，触发缺页异常而实现调入的。CPU会将PC指令计数器中的逻辑地址送入MMU，让MMU通过地址转换。MMU通过查快表、如果快表未命中后查页表，页表也未命中后触发缺页异常处理程序，最终同时保证访存的正文段页已被调入内存且物理地址已经合成。MMU将物理地址送入地址总线取正文段的一条指令。在本人的设计中正文段占4页，也就是共有4页指令，为了达到每一页正文段都能在进程的生命周期中被调入内存，本人在前三页的每一页中都编写了一个跳转指令，可以跳转到下一页。

#### 数据段
	
数据段存放的是进程私有的运行所用数据。数据段在本人的设计中，由访存指令进行访存，且中共具有两页，其中每一页都有可能被访存指令访存。同样根据IR寄存器中的访存指令提取出逻辑地址偏移，与当前进程的逻辑空间始地址结合，得到访存数据段中某一数据存储单元的逻辑地址，通过MMU地址转换，得到物理地址。中途也可能触发缺页异常。事实上，无论是正文段第一次取指或数据段第一次取数都必然会触发缺页异常。然后在内存中对应物理地址取数。

#### 用户缓冲区

用户缓冲区的主要作用就是临时存放来自外设传入给该进程的数据和和该进程送给外设的数据。在本人的设计中，用户缓冲区的主要作用就是为了实现IO指令。用输入指令举例。当进程执行输入指令阻塞自己，系统启动DMA为其传输数据后，系统缓冲区就被灌满，该进程再次回到CPU后，会触发系统的输入善后函数，同时再次阻塞。此时系统会将胸痛缓冲区中的数据送入用户缓冲区暂存。

#### 核心栈
	
核心栈是进程用来保存现场的又一方案，在教材75页的进程映像组成部分中详细介绍了核心栈的内容。从中我们可以看到无论是在系统异常还是硬件中断时，都要使用进程核心栈保存现场。所以在本人的设计中也遵循了这一知识点。在进程运行时就会先通过恢复现场后栈顶指针SP会恢复到栈底。当发生异常或硬件中断时都会通过SP寄存器和用户核心占配合将CPU的关键寄存器值全部压入核心栈中。当再次运行时，如果判断其是否阻塞过，如果是，则通过使用核心栈和SP指针配合将现场信息弹出放入CPU中，达到恢复现场。

在本人的设计中核心栈占用１页的空间。

### 3.5	保存现场

上文提到无论是异常还是硬件中断都需要用核心栈保存现场。其中缺页异常和系统调用都属于异常，也就是内中断。而外中断包括IO中断（DMA发出的中断信号），时钟中断（时钟发出的时间片耗尽信号）以及硬件故障。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/中断分类.png)

在此次课程设计中，没有办法模拟硬件故障，所以摒弃该情况。综上所述，无论是异常还是硬件中断都要将现场保存在核心栈中。保存后再进行CPU转为内核态和进程的阻塞。例如当进程执行一条访存指令，它将触发系统的P操作，替它申请目的寄存器，此时，就应当用核心栈保存现场，再转为内核态，阻塞进程，系统执行P操作。

这与PCB保存现场并不矛盾。因为实际上执行阻塞进程的原语的过程中就包含了PCB保存现场的过程。本人实现的进程阻塞以及进程因为时间片耗尽回到就绪队列队尾时都有用PCB保存现场的操作。即如果是运行回到就绪，只用PCB保存现场信息。如果时运行进入阻塞，就提前在执行阻塞原语前用核心栈保存现场，然后在执行阻塞原语的过程内用PCB再保存一遍现场。这样做不仅符合了教材８２页中对阻塞原语的描述，也为现场的恢复提供了便利条件。

### 3.6	恢复现场

事实上，进程的阻塞是因为中断。所以如果被阻塞进程再次唤醒进入就绪队列排队，再回到CPU时，只需要判断它是否在之前阻塞过就可以断定该进程是否需要在用PCB恢复现场后再用核心栈恢复了。

之所以上文中说两次保存现场为现场恢复提供遍历是因为，本人在设计恢复现场时，无论进程是否阻塞过，都会先用PCB恢复一次现场。此时恢复现场后，SP的值一定上一次离开CPU时保存的值。如果进程只是因为耗完时间片而离开CPU，那么SP应该指向该进程的核心栈底，即核心栈为空，没有保存现场。如果是因为中断而被阻塞，那么PCB保存现场时记录的SP内容则是SP指向栈顶，即核心栈满，说明核心栈保存了栈底。所以当该进程进入CPU后，先用PCB恢复现场，如果之前阻塞过，此时用SP就可以直接弹栈，而不用使用其他任何存储方式存储进程上一次SP所在的位置。

### 3.7	复杂页表

关于页表的设计，我再多方探寻后发现目前存在两种结构的页表，一种是页表外页表方式，还有一种是本人实现的复杂页表结构。
	
页表外页表中，页表承担的功能是逻辑页号和内存物理页号的对应。而外页表则负责外存地址和逻辑地址的对应。本人所采用的是复杂页表结构，它将上述两种对应关系聚合在一张页表上。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/页表.png)

首先要想将逻辑页、物理页框、外存扇区的对应关系都体现在一张表上肯定长度不够，不能使用１６位存储单元存储一个表项。

由于逻辑空间的大小由总线位数决定，按照设计要求，总线位数为１６位，而一页的寻址空间总共由５１２B，即２的９次方个字节，如果按照单字节索引方式，那么偏移位９位，如果按照双字节存储，一般的做法是将９位偏移的最低位规定为０，就可以访存双字节。而偏移为９位，则页号一定位７位这样就可以拼接成１６位的逻辑地址。

而事实上，物理内存的设计要求只有６４个页框，所以物理地址最多是６位。

状态位标识该逻辑页是否已经调入物理内存。

访问字段是供LRU算法使用的。

修改位标识该逻辑页所对应的物理页框内的数据是否被修改过，如果状态位为“假”，那么该位无效。

外存地址１１位是由于外存总共由２０４８个扇区组成，即２的１１次方个扇区，而对外存的操作都是块操作，所以唯一确定一个扇区需要１１位。

所以最终确定使用３２位标识一个页表项。此外快表TLB是页表的一个子集，所以TLB中的表项结构与此一致。

### 3.8	伙伴算法数据结构

伙伴算法的功能是——管理物理内存，那有可以细分为两个功能：申请空闲块功能和释放占用块的功能。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/伙伴算法.png)

首先中间的方块是空闲块链表的表头，它组成一个数组，数组长度为要分配内存范围转化为2的幂后的指数部分加1，比如本系统中要分配的物理内存大小为32块，那么该数组的长度为6。


表头右侧指的是空闲块链表，而左侧指的是位示图，位示图中的位数从小到上逐渐指数上升。最下层位图位数为2的0次方，但是每一位表示的是2的5次方个页框。最上层位图位数为2的5次方，但是每一位表示的是2的0次方个页框。

使用伙伴算法每次只能申请2的整数幂个页框。也就是占用某一层位示图中的1位。此外相邻的两个位还会归并为下一层的一个位。

本人将该算法应用于缺页中断的调入新页面放入新的物理页框以及创建进程时开辟进程缓冲区和进程核心栈。

### 3.9	外存位示图

外存使用位示图的方式管理２０４８个扇区。

虽然本人并未实现文件系统的设计，但是采纳了书中对于文件系统超级块中存放位示图的思路，设计了一个较为简单的外存扇区管理办法，但是也加入了本人的改进：如上文对辅存的介绍中辅存分为两个部分，交换区和文件区，本人在交换区和文件区分别设置两个位示图，并提供了申请和释放外存扇区的功能，

### 3.10	进程原语
#### 3.10.1	进程创建

在创建作业前首先应该检测PCB池中是否还有空白的PCB可供使用，如果没有则拒绝进程的创建。

在创建时首先为其寻找一个独一无二的进程ID，本人设计的是找到当前PCB池中最大的进程ID号，然后加一。

根据要创建进程的JCB，将分配得到的PCB进行初始化。先确定一个进程映像有多大：确定正文段的页数、确定数据段的页数、确定用户缓冲区和核心栈各需要多少页。根据确定的总页数通过页表分配一段连续的逻辑空间页。这里本人将“最佳适应算法”移植到了逻辑页的分配上。首先我通过遍历PCB池，得到页表中空闲区间都有哪些，然后将这些空闲区见从小到大排列，找到正数第一个能够容纳进程映像占用需求的区间即可。

此外还需要开辟交换区空间，使用的方法就是上文提到的位示图申请方法。上述操作后我们起始得到了进程的逻辑空间大小和逻辑空间始地址，以及交换区大小和交换区首地址，将这些参数全部赋值给PCB。

在申请占用了逻辑空间后，还需要为该进程专属的页表的辅存字段进行初始化。首先进程开始运行的时候，正文段和数据段不会因为进程的创建而把作业文件区对应的作业指令集和数据集立刻搬运到交换区，然后通过缺页异常处理程序一页一页调入物理内存。原因是如果我们从外存和内存整块搬运，花费的代价较大。经过多方求证，本人得出的结论如下：

1)	进程的正文段和数据段在第一次执行的时候都是在作业文件区中的对应作业的程序文件和数据文件上，而逻辑地址映射文件区的这些页面，而非一开始就是交换区。

2)	缺页中断时可以把文件区的指令集调入内存。

3)	进程在执行时，首先将程序文件的“指令集”和“数据部分”通过缺页中断调入内存，这两部分并不在交换区，而是在文件区。而这两部分调入后，装入了指令集的内存就是正文段；装入了“数据部分”的内存就是数据段；这两个段和新开辟的核心栈共同构成了“进程映像”。

4)	当正文段或数据段的某一页在缺页中断时由LRU算法被替换出内存时，应当将这一页放入交换区。此时应该重新修改逻辑页号与外存地址的对应情况——将之前该页的逻辑页号与文件区程序的外存地址的对应，改为该页逻辑页号与交换区刚刚覆盖的这一个扇区的外存地址的对应。

5)	正是在这一次次的缺页中断或挂起导致的页面替换中，内存中的进程映像才不断被从内存搬移到交换区中。

6)	最后可以达到书上说的“交换区是进程映像保存的位置”的效果。这个效果并不是一开始我误解的“创建时一次性从文件区搬运到交换区”达到的，而是在一次次替换后达到的。

综上所述，进程页表的辅存字段一开始对应文件区的扇区地址，且创建进程时无需搬运文件区的指令集和数据集到交换区。

然后时预调页过程，也就是将进程映像中的用户缓冲区和核心栈在内存中开辟。同时更新页表的物理页号以及外存地址字段。因为这两个部分时开辟而非调入，所以外存地址字段应该为交换区的对应位置。最后将初始化后的PCB加入到就绪队列中等待运行。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/操作系统报告流程图-创建进程.png)

#### 3.10.2	进程撤销

当进程的指令集执行完毕后撤销。进程的撤销包括几个步骤：首先释放交换区，使用的是上文外存位示图法中提到的释放交换区扇区的函数，然后通过伙伴算法释放全部内存块，放弃进程占用的页表，将PCB加入到PCB池。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/操作系统报告流程图-撤销进程.png)

#### 3.10.3	进程阻塞

进程阻塞是在进程运行过程中遭遇异常时发生的。包括进程用PCB保存现场，给PCB设置阻塞原因，然后将PCB放入阻塞队列。

#### 3.10.4	进程唤醒

当进程等待的事件发生时，该进程将被唤醒。包括两种唤醒，如果时挂起阻塞，就唤醒为挂起就绪。如果是阻塞，就唤醒为就绪。具体操作都是设置PCB的状态为就绪（挂起就绪），将PCB从当前阻塞队列（挂起阻塞队列）移动到就绪队列（挂球就绪队列）。

#### 3.10.5	进程挂起

当有效的物理页框数达到一个阈值时，将认为内存资源不足，挂起PCB中控制信息的访问字段最大的进程。主要操作有：将进程再内存中的物理块全部挂到外存对应进程的交换区中，并且将状态改为挂起，然后将PCB放入对应的挂起队列：阻塞进程变为挂起阻塞，并且将PCB挂起到挂起阻塞队列；就绪进程则变为挂起就绪，并且将PCB挂起到挂起就绪队列。

#### 3.10.6	进程激活

当有进程撤销使得内存中有效页框数减少到低于阈值后，挂起进程可以被激活。主要操作有：将状态改为就绪或阻塞，然后将PCB放入对应的队列：挂起阻塞进程变为阻塞，并且将PCB移到到阻塞队列；挂起就绪进程则变为就绪，并且将PCB移到到就绪队列。

#### 3.10.7	P操作

当进程在运行某条用户指令时，如输入指令，进程可能为了执行该指令而需要拥有外设或者是系统缓冲区资源，才能继续运行。而获得外设等资源需要系统执行P操作。如果该资源数量在自减1以后，为一个小于0的数，那么，说明该资源被其他资源占用完，所以该进程的进程标识会被排到它申请的信号量的队尾，同时该进程也会在就绪队列一直阻塞。

#### 3.10.8	V操作

当进程当前执行的指令所需要完成的任务完成后，应当释放自己用来执行指令的资源，如输入指令中当系统缓冲区的数据已经全部搬运到指定的系统缓冲区中，那么相当于输入指令的实际任务已经完成。接下来就应当释放该进程未来实现输入所占用系统缓冲区和外设这两类资源。这是就要阻塞自己，触发系统V操作。

具体操作为：首先为要释放的信号量的数量自加1，如果得到的值是一个非正数，那么意味着有进程在等待该资源。所以应当换新排在该资源等待队列的第一个进程。

### 3.11	特权指令集

所谓特权指令集是指只有系统能够执行的程序

#### 3.11.1	缺页异常及处理程序

分情况讨论：

##### 替换情况

这要涉及到驻留集，就是进程在虚存中的合法页集合。即进程可以有很多的逻辑页，但并不是所有的逻辑页都在物理内存中，而只有一部分逻辑页的数据能调入物理内存中。而驻留集就是说最多允许一个进程有多少块自己的数据能够调入到物理内存中。比如驻留集大小为8，那这个进程最多只能把自己的众多逻辑页的数据中挑选8页数据从外存调入到内存中。而剩下的只在外存存着。

交换区是进程映像的全部，也是逻辑地址的全部寻访空间，而存储在物理内存用户区的有效页内的数据，实际上是交换区的部分页内数据在物理内存中的拷贝。也就是说，根据逻辑地址你可以寻访到任何一个交换区的任何一个位置，而如果这一逻辑页的有效位为1，那就说明，它在物理内存中有拷贝，根据页表查到物理内存页号，直接去访问物理内存就可以了，不需要再将对应这一逻辑页的外存扇区内容写入物理内存再访问了。

介绍完驻留集，情况是当进程的驻留集满了，还要调入新的一页，那就要替换页面。

就是根据页表的访问字段，找到访问字段最大的那一页替换，因为本人设计的是局部替换，所以找到进程对应的页表项之后，就将它的有效位置为false，然后将它的物理块号字段取出来放到要调入的逻辑页的那一个页表项的相应字段，然后把这一项设为有效。

##### 填充情况

该进程在自己驻留集还未达到限制，比如驻留集设限为8块有效块，而目前的有效块还没有到8块，就还可以直接将外存对应块内的数据直接填充进物理内存。这就是填充情况。

这里涉及到伙伴算法（上文介绍过）。

申请到了这一块物理内存块，就把对应页表项的物理块号字段设为这个块号，然后将有效位设置为true。同时将这一页表项复制到快表的队尾，这里就要再介绍一下我的MMU内部的快表的替换算法——有很多同学可能再设计快表时不知道怎么替换或者插入快表项，那在教材208页就有提到，上面说：

系统需要淘汰旧的快表项，最简单的策略是“先进先出”，总是淘汰最先登记的页面。每次运行态的进程变更就要清空快表重新填充，参考了教材218页有：在重新装载MMU的页表基址时，就会清空快表重填。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/缺页中断.png)

#### 3.11.2	DMA赋值和启动程序

DMA赋值分别要将系统缓冲区的地址、用户缓冲区的地址，输入方向、传输字数赋值给DMA。然后想DMA发送启动信号，DMA的线程会感知到CPU的消息并进行工作。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/raw/master/pictures/操作系统课程设计设计图-DMA.png)

#### 3.11.3	输入善后处理

输入的善后处理是指DMA向CPU发送自己已经执行了从外设将数据搬运到系统缓冲区中断信号，系统接到这个信号后，执行IO中断处理程序（后文介绍），唤醒了等待DMA传输的进程，然后进程回到CPU运行，阻塞自己，触发系统调用，系统会执行该程序，具体操作为：将系统缓冲区的数据搬运到用户缓冲区，仅此而已。

#### 3.11.4	时钟中断处理程序

时钟中断处理即当时钟发送信号给CPU告知CPU时间片耗尽，那么系统将当前在CPU中运行的进程强行取出放入就绪队列的队尾。

#### 3.11.5	IO中断处理程序

IO中断是指DMA在启动后与CPU同时处在工作状态，DMA一直执行数据的搬运，当DMA搬运数据结束后，向CPU发出硬件信号，告知CPU自己执行完了搬运工作。CPU会冻结当前执行的无关进程，执行该信号的处理程序。具体操作为唤醒等待DMA输入或输出的那个进程。然后继续执行冻结的无关进程。

#### 3.11.6	死锁检测及恢复程序

当时钟计数时间时发现到来死锁检测的时间，也会向CPU发送硬件信号。系统会检测到该信号并开启死锁检测算法。

至于死锁恢复：释放第一个死锁进程的全部资源满足其他等待这些资源的进程最后回滚一条指令然后唤醒自己重新执行。

## 4	程序结构及模块功能的实现

本人的程序结构分为6个线程：DMA线程、作业管理子系统线程、进程管理子系统线程、界面刷新线程、请求-作业线程、时钟线程。

### 4.1	时钟线程

时钟主要负责三项工作：报告时间片到，报告作业检测时间到，报告死锁检测时间到。

### 4.2	请求作业线程

负责在请求到来时间将请求创建为作业。

### 4.3	界面刷新线程

不断刷新界面让可视化动起来。

### 4.4	进程管理子系统

负责进程在系统中执行指令，并根据指令的驱动进行低级调度（三态转换）和中级调度（挂起与激活）。

### 4.5	作业管理子系统

负责对作业的管理（高级调度），将作业后备队列中的作业创建为进程参与系统调度。

### 4.6	DMA线程

负责执行CPU发来的数据传输工作，与CPU并发执行。


![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/DMA操作系统课程设计设计图-第11页.png)

## 5	实践心得
### 5.1 设计贴近理论

对本此操作系统课程设计，我在设计过程中尽可能地以教材为依据，进行最为基础最为贴近理论地设计。并在3个月的课程设计期间，稳步实现硬件仿真、作业管理、存储器管理、进程管理。最终实现可视化，其中内存的可视化，本人别出心裁地设计出以伙伴算法中地位示图为原型地内存界面。另外，在进程管理中，本人与小组讨论成员共同设计了复杂而细致的用户指令集和特权指令集，也成为了系统的一大特色。

### 5.2 讨论的价值

遇到自己疑惑的部分，我都会和我讨论组的同学进行探讨，有时探讨时间长达2~3个小时，如果对于课本上的一些知识还是无法理解掌握，或者是对于设计中的迷惑还是没有办法理清，就进一步询问老师和学长们在设计方面的意见。在与他们的探讨之后，我的设计思路变得更加的透彻。

所谓“理不辨不明”，经常性地在深夜还在就一个设计进行争论是我的操作系统设计中一个重要地组成部分。当然，讨论时有价值的，几乎每一次讨论后都能使我在操作系统的知识体系中得到全新的收获。
对设计负责到底

在最后开始自我测试的阶段，我发现了自己设计中只设计了自动生成作业的操作并没有设计从外部读案例的机制。这给我带来困扰，因为自我测试就必须要求我的系统拥有读入已有测试用例的功能，不可能只依赖自动生成来触发。所以我的设计是不合理的，尽管已经到了测试的阶段才发现这个问题，改动可能会对整个系统带来不可估量的影响。

但是本着对自己作品负责到底的态度，毅然决定临时进行更改，经过争分夺秒地分站终于实现了令本人满意的作品。

仍然有改进空间

但没有设计是绝对完美的，其实还存在一个小缺陷，那就是运行时间太长，为了运行复杂的指令集，并且支持８个进程运行，所以该系统撤销全部进程需要很长时间。
以上就是本人的实践心得。

## 安装及使用

### 安装

下载test文件夹，

将其中的HNOS3.rar压缩包文件解压；

![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/1.png)

解压后会看到一个以“HNOS3”命名的文件夹，然后点击进入该文件夹；

![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/2.png)

在保存该文件夹的文件管理器界面的文件路径框中单击鼠标左键；

然后键入“cmd”字符串，输入回车，会弹出Windows系统的控制台程序；

![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/3.png)

输入java -jar HNOS3.jar后按下回车，进入仿真系统；

![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/4.png)

到此，安装完成。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/5.png)

### 使用

如果想要使用测试用例，请在程序界面打开后，到HNOS3文件夹找到input文件夹。

点击进入该文件夹，将内部我之前测试留下的文件都删除。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/6.png)

到HNOS3文件夹的外部找到准备的三个用例，然后选择其中一种用例，打开其文件夹。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/7.png)

比如选择“死锁用例”文件夹，单击打开内部，将内部所有文件复制到input文件夹中。

（注意不是将文件夹放进input文件夹，而是将文件夹内的全部文件放入input文件夹。）

![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/8.png)

然后重新调出程序界面，点击“选用外存文件”按钮，会发现请求序列变为用例的请求序列，其实已经将用例读入。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/9.png)

点击系统启动即可。中途可以通过点击创建请求来创建随机生成的请求（建议在测试用例时不要这么做，因为新请求的进入并创建为进程会打乱本人提供原有用例的触发效果如时间差导致死锁效果失效。）

### 其他功能

#### 创建请求按钮

点击创建请求按钮，只要创建作业总数没有超过8都可以继续创建作业，如果超过8个，将因为外存作业文件区大小限制拒绝创建。

#### 自动生成作业功能

另外的，在没有点击过“选用外存文件”按钮就直接点击“系统启动”按钮可以自动生成五个作业的指令集和数据集，并且顺利运行运行。中途当然也可以点击“创建请求“按钮来创建一个新的请求。

注意：本人设计的外存作业文件区只占用一个磁道，所以如果前后所有请求总数超过8个，则超过的请求将被拒绝创建为作业。

#### 自动生成死锁案例

在没有请求被创建为作业时，点击死锁案例按钮，会自动为用户生成一对可以死锁的进程。

#### 主存按钮

点击后会弹出使用伙伴算法的位示图部分显示出的内存分配和回收情况。一列代表一个页框的情况。一列全部变为灰色表示被装用。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/10.png)

要声明一点，前0~31号属于系统区固定的页框，所以只要系统在运行，前10块都会被占用。不会被回收。

32~63号属于用户区使用，只要进程全部撤销，最后一定会32个块全部回收，达到最后一行全绿的效果。

#### 页表和快表按钮


点击页表按钮会弹出可视化后的页表。如下图所示。同样地，点击快表按钮，会弹出可视化后的快表。

![add image](https://github.com/Nirvana-fsociety/OSsimulation/blob/master/pictures/11.png)

