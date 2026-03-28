import React from 'react';

import styles from './index.module.scss';

const Component = () => {
  return (
    <div className={styles.body}>
      <div className={styles.ambientDecoration} />
      <div className={styles.overlayBlur} />
      <div className={styles.main}>
        <div className={styles.greetingSection}>
          <div className={styles.heading2}>
            <p className={styles.text}>Good Morning!</p>
          </div>
          <div className={styles.container}>
            <p className={styles.text2}>Ready for your deep work session?</p>
          </div>
        </div>
        <div className={styles.bentoLayoutForConten}>
          <div className={styles.pomodoroTimerCard}>
            <p className={styles.text3}>Current Focus</p>
            <div className={styles.overlayBlur2} />
            <div className={styles.circularCountdownCon}>
              <div className={styles.container2}>
                <p className={styles.text4}>25:00</p>
                <div className={styles.margin}>
                  <p className={styles.text5}>Deep Focus</p>
                </div>
              </div>
              <img src="../image/mn2vq7kq-4kc1r6i.png" className={styles.sVg} />
            </div>
            <div className={styles.controls}>
              <div className={styles.button}>
                <img
                  src="../image/mn2vq7kq-lrmck4d.svg"
                  className={styles.container3}
                />
              </div>
              <div className={styles.button2}>
                <div className={styles.buttonShadow}>
                  <p className={styles.text6}>
                    Start
                    <br />
                    Session
                  </p>
                </div>
              </div>
              <div className={styles.button3}>
                <img
                  src="../image/mn2vq7kq-01wih17.svg"
                  className={styles.container4}
                />
              </div>
            </div>
          </div>
          <div className={styles.sidebarWidgets}>
            <div className={styles.nowPlayingWidget}>
              <div className={styles.nowPlayingWidgetShad}>
                <div className={styles.background}>
                  <div className={styles.overlayShadow}>
                    <img
                      src="../image/mn2vq7kq-iqo4lws.svg"
                      className={styles.container5}
                    />
                  </div>
                </div>
                <div className={styles.container6}>
                  <p className={styles.lofiStudyBeats}>Lofi Study Beats</p>
                  <p className={styles.aestheticFocusRadio}>
                    Aesthetic Focus Radio
                  </p>
                </div>
                <div className={styles.button4}>
                  <img
                    src="../image/mn2vq7kq-f1ao1r8.svg"
                    className={styles.container7}
                  />
                </div>
              </div>
            </div>
            <div className={styles.todaySPlan}>
              <div className={styles.todaySPlanShadow}>
                <div className={styles.container8}>
                  <p className={styles.text7}>Today's Plan</p>
                  <div className={styles.backgroundShadow}>
                    <p className={styles.text8}>3 TASKS</p>
                  </div>
                </div>
                <div className={styles.container11}>
                  <div className={styles.taskItem1}>
                    <div className={styles.taskItem1Shadow}>
                      <div className={styles.border} />
                      <p className={styles.text9}>Review design system specs</p>
                    </div>
                  </div>
                  <div className={styles.taskItem2}>
                    <div className={styles.taskItem2Shadow}>
                      <div className={styles.background2}>
                        <img
                          src="../image/mn2vq7kq-cjmip3k.svg"
                          className={styles.container9}
                        />
                      </div>
                      <div className={styles.container10}>
                        <p className={styles.text10}>Prepare focus deck</p>
                      </div>
                    </div>
                  </div>
                  <div className={styles.taskItem3}>
                    <div className={styles.taskItem3Shadow}>
                      <div className={styles.border} />
                      <p className={styles.text11}>Final UI polishing</p>
                    </div>
                  </div>
                </div>
                <div className={styles.button5}>
                  <img
                    src="../image/mn2vq7kq-x5rffn2.svg"
                    className={styles.container12}
                  />
                  <p className={styles.text12}>Add Task</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Component;
