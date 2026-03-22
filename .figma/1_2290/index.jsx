import React from 'react';

import styles from './index.module.scss';

const Component = () => {
  return (
    <div className={styles.tabBar}>
      <div className={styles.tabs}>
        <div className={styles.tabBarItem}>
          <img
            src="../image/mmjdujil-yafldvm.svg"
            className={styles.iconTabHomeFill}
          />
        </div>
        <div className={styles.tabBarItem2}>
          <img
            src="../image/mmjdujil-mkzwinz.svg"
            className={styles.iconTabHomeFill}
          />
        </div>
        <div className={styles.tabBarItem3}>
          <img
            src="../image/mmjdujil-whdas8y.svg"
            className={styles.iconTabHomeFill}
          />
        </div>
        <div className={styles.tabBarItem4}>
          <img
            src="../image/mmjdujil-3jmtcvs.svg"
            className={styles.iconTabHomeFill}
          />
        </div>
        <div className={styles.tabBarItem5}>
          <img
            src="../image/mmjdujil-dt2buva.svg"
            className={styles.iconTabHomeFill}
          />
        </div>
      </div>
      <div className={styles.homeIndicator2}>
        <div className={styles.homeIndicator} />
      </div>
    </div>
  );
}

export default Component;
