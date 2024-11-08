package student.inti.signuplogin;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

public class TaskId {
    //this will exclude with the firebase data
    @Exclude
    public String TaskId;

    public <T extends TaskId> T withId(@NonNull final String TaskId){
        this.TaskId = TaskId;
        return (T)this;
    }


}
