/**
 * Description: This class is used mostly to create ArrayLists so that the objectID
 * can be grabbed. The thought was that instead of querying on each screen we can refactor
 * this so that the object is passed to each activity, but this wasn't implemented yet
 *
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

public class Course {
        String objectID;
        String courseName;
        String courseLocation;
        String courseTime;
        
        public Course(String courseName, String objectID) {
                this.courseName = courseName;
                this.objectID = objectID;
        }
}