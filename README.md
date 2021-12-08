_______________
# Requirements:
_______________

- Java 1.7+
- Eclipse OCL 6.0.1+


This repository contains all the necessary scripts and inputs we used to evaluate  `TD-SB-temPsy` tool.
The repository contains `2` major steps: 
- Data Preprocessing: traces generation and interpolation from `Preprocessing` folder, based on raw traces and unique signals defined in these traces.
- Tool execution: execution of `TD-SB-TemPsy` tool from `lu.svv.offline` folder, given the preprocessed/interpolated traces (`.csv` format) and temporal properties (`.xmi` format). 

_____________________
# Data Preprocessing:
_____________________

Our data preprocessing is implemented in class `TracesPreprocessorAndInterpolator.java` located under `Preprocessing` folder.
The preprocessing is essentially based on two steps:
_______________
- Traces Merge:
_______________

The merge code is written in class `TracesMerger.java`.
- Function `mergeLogsBasedOnSimulationDuration` takes in our raw traces `RawTraces` folder and returns merged traces stored under a generated folder called `MergedTraces`. The merge consists of merging all the logs from the input raw traces (represented in a `.tsv` format).
- Function `writeToStatisticsFile` is used to compute the merge statistics (w.r.t number of entries and the simulation duration per merged log). The latter calls `createMergeStatisticsFile` and `addInformationAfterTracesMerge` functions sdefined under `TraceStatistics`class.

______________________________
- Merged Traces Preprocessing:
______________________________
The preprocessing inputs are:
- The merged traces (generated from the previous step), that we store under `Preprocessing/MergedTraces` folder.
- The `xmi` properties stored under `XmiProperties` folder.

Our preprocessing is implemented in `TracesPreprocessorAndInterpolator` class. 
Similarly to the traces merger, we first preprocess the traces then we compute the preprocessing statistics. 
- Preprocessing: 
It is based on 5 functions as follows:
    - `removeUselessAttributesAndConvertTimestamps`: removes the useless columns and converts the date format to microseconds from the raw traces generated under `MergedTraces` folder. The output traces are stored under `PreProcRawData` folder.
    - `getTracesToInterpolate`: for each `xmi` property from the properties folder `XmiProperties`, we first retrieve all the unique signals from the property definition, then we loop over the preprocessed traces from `PreProcRawData` folder. For each of these traces, we store all the records from that trace that concern the unique property signals only to a new trace that we store under `CsvLogs` folder. It results that each `xmi` property can come with many generated `csv` traces from `CsvLogs` folder. We then generate a match file `match.txt` that we store under the generated folder `Match`. The file contains all the `properties/traces` possible combinations.
    - `sort`: used to sort the preprocessed traces from `CsvLogs` folder, based on the timestamp column.
    - `applyInterpolation`: for each of the preprocessed traces under `CsvLogs` folder, we apply an interpolation function in order to deal absence of records for missing timestamps. We implemented `2` different interpolations. The choice of the interpolation depends on the required interpolation from each signal definition, from each `.xmi` property. Our interpolation functions are the following:
        - A `linear` interpolation.
        - A `piecewise-constant` interpolation, based on the last-seen signal value.
    - `mergeRecordsByTimestamp`: the function is used to merge all rows from each log from `CsvLogs` folder based on similar timestamps.
    - `getFinalLogs`: used to keep the required columns only in each preprocessed trace. It produces the final traces that we need for our trace diagnostics approach later.
    - `evaluateComplexSignals` and `getFinalTracesWithComplexSignals` are functions used evaluate complex signals, represented as a mathematical expression.


_________________
# lu.svv.offline:
_________________

To run `TD-SB-TemPsy`, one needs `2` types of inputs:
- properties written in an `.xmi` format, stored under `XmiProperties` folder and
- preprocessed/interpolated traces written in a `.csv` format, stored under `CsvLogs` folder.
Two more folders are needed to run our tool: 
- `lib`: contains the `sb-tempsy-report.ocl` file that contains the full implementation of the violation causes/ diagnoses semantics definitions. 
- `models`: contains the `4` following `ecore` models:
    - `sbtempsy.ecore`: a model generated from our `DSL` `TD-SB-TemPsy-DSL` syntax, 
    - `trace.ecore`: a model that defines the data structure of our traces and
    - `diagnostics.ecore`: a model that contains the full definition of all the violation types supported by `TD-SB-TemPsy` tool and 
    - `check.ecore`: a model that is used to generate artefacts based on the three models aforementioned.
Finally, the main class for running our tool is the following:
- `SingleDiagnosticsMain.java`: the main class required to report diagnosis for a single violated property over a single trace. 


`TD-SB-TemPsy` tool can be ran using `2` different methods:
    - from the `IDE` (e.g., `Eclipse` environement), the arguments in lines `10` and `11` shall be replaced by the absolute paths of 
the property and the trace, respectively.
    - Example: 
        String propertyFile = "./XmiProperties/PropertyExample.xmi";
		String traceFile = "./RawTraces/traceExample1.csv";
    - as a standalone, using a `jar` file, where the absolute paths of the property and the trace shall be 
    written in the commandline.

Copyright by University of Luxembourg 2020-2021. 

Developed by **[Chaima Boufaied](https://wwwfr.uni.lu/snt/people/chaima_boufaied)**, <chaima.boufaied@uni.lu> University of Luxembourg.

Developed by **[Claudio Menghi](https://wwwfr.uni.lu/snt/people/claudio_menghi)**, <claudio.menghi@uni.lu> University of Luxembourg.

Developed by **[Domenico Bianculli](https://wwwfr.uni.lu/snt/people/domenico_bianculli)**, <domenico.bianculli@uni.lu> University of Luxembourg. 

Developed by **[Lionel Briand](https://wwwfr.uni.lu/snt/people/lionel_briand)**, <lionel.briand@uni.lu> University of Luxembourg.
