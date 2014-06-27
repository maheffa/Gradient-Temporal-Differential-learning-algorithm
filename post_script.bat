SET Controller_files=(RobotController Arm Base Gripper SuperController Util TinyMath)
SET SuperController_files=(SuperController Datas Demon Agent ControlPanel QStatePanel Util TinyMath)

SET Project_Path="D:\Workspace\Freelancing\webots_project\youbot0\"
SET Controller_Path=%Project_Path%\controllers\

FOR %%f IN %Controller_files% DO COPY src\%%f.java %Controller_Path%\RobotController\ /Y
FOR %%f IN %SuperController_files% DO COPY src\%%f.java %Controller_Path%\SuperController\ /Y