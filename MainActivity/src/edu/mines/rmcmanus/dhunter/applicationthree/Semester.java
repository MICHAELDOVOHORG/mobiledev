/**
 * Description: This class is used mostly to create ArrayLists so that the objectID
 * can be grabbed. The thought was that instead of querying on each screen we can refactor
 * this so that the object is passed to each activity, but this wasn't implemented yet
 *
 * @author Ryan McManus, David Hunter
 */

package edu.mines.rmcmanus.dhunter.applicationthree;

public class Semester {
        String semesterType;
        String semesterYear;
        String objectId;
        
        public Semester(String semesterType, String semesterYear, String objectId) {
                this.semesterType = semesterType;
                this.semesterYear = semesterType;
                this.objectId = objectId;
        }
}
