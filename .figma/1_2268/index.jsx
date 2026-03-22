import React from 'react';

import styles from './index.module.scss';

const Component = () => {
  return (
    <div className={styles.pills}>
      <div className={styles.iconLabel}>
        <img src="../image/mmjdujiv-qymo3p7.svg" className={styles.iconFavorite} />
        <p className={styles.label}>Favorites</p>
      </div>
      <div className={styles.iconLabel2}>
        <img src="../image/mmjdujiv-b3ndrgu.svg" className={styles.iconFavorite} />
        <p className={styles.label2}>History</p>
      </div>
      <div className={styles.iconLabel2}>
        <img src="../image/mmjdujiv-5ou0u0g.svg" className={styles.iconFavorite} />
        <p className={styles.label2}>Following</p>
      </div>
      <div className={styles.iconLabel2}>
        <img src="../image/mmjdujiv-rjx2jcu.svg" className={styles.iconFavorite} />
        <p className={styles.label2}>Orders</p>
      </div>
    </div>
  );
}

export default Component;
