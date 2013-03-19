SET Controller_files=(MyController Arm Base Gripper SuperController Util)
SET SuperController_files=(SuperController InstructionFitnessFunction GeneticAlgorithm Util)

SET Project_Path="D:\Workspace\Freelancing\webots_project\youbot0\"
SET Controller_Path=%Project_Path%\controllers\

FOR %%f IN %Controller_files% DO COPY src\%%f.java %Controller_Path%\MyController\ /Y
FOR %%f IN %SuperController_files% DO COPY src\%%f.java %Controller_Path%\SuperController\ /Y